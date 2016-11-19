#!/usr/bin/env perl

# Heavily based on the script from:
# check_mem.pl Copyright (C) 2000 Dan Larsson <dl@tyfon.net>
# heavily modified by
# Justin Ellison <justin@techadvise.com>
#
# The MIT License (MIT)
# Copyright (c) 2011 justin@techadvise.com

# Permission is hereby granted, free of charge, to any person obtaining a copy of this
# software and associated documentation files (the "Software"), to deal in the Software
# without restriction, including without limitation the rights to use, copy, modify,
# merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
# permit persons to whom the Software is furnished to do so, subject to the following conditions:

# The above copyright notice and this permission notice shall be included in all copies
# or substantial portions of the Software.

# THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
# INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
# PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE
# FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT
# OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
# OTHER DEALINGS IN THE SOFTWARE.

# Tell Perl what we need to use
use warnings;
use strict;
use Getopt::Std;

#TODO - Convert to Nagios::Plugin
#TODO - Use an alarm

# Predefined exit codes for Nagios
use vars qw($opt_c $opt_f $opt_u $opt_w $opt_C $opt_v %exit_codes);
%exit_codes   = ('UNKNOWN' ,-1,
        		 'OK'      , 0,
                 'WARNING' , 1,
                 'CRITICAL', 2,
                 );

# Get our variables, do our checking:
init();

# Get the numbers:
my ($free_memory_kb,$used_memory_kb,$caches_kb) = get_memory_info();
print "$free_memory_kb Free\n$used_memory_kb Used\n$caches_kb Cache\n" if ($opt_v);

if ($opt_C) { #Do we count caches as free?
    $used_memory_kb -= $caches_kb;
    $free_memory_kb += $caches_kb;
}

# Round to the nearest KB
$free_memory_kb = sprintf('%d',$free_memory_kb);
$used_memory_kb = sprintf('%d',$used_memory_kb);
$caches_kb = sprintf('%d',$caches_kb);

# Tell Nagios what we came up with
tell_nagios($used_memory_kb,$free_memory_kb,$caches_kb);


sub tell_nagios {
    my ($used,$free,$caches) = @_;
    
    # Calculate Total Memory
    my $total = $free + $used;
    print "$total Total\n" if ($opt_v);

    my $perfdata = "total=${total}KB used=${used}KB free=${free}KB caches=${caches}KB";
    
    if ($opt_f) {
      my $percent    = sprintf "%.1f", ($free / $total * 100);
      if ($percent <= $opt_c) {
          finish("CRITICAL - $percent% ($free kB) free|$perfdata",$exit_codes{'CRITICAL'});
      }
      elsif ($percent <= $opt_w) {
          finish("WARNING - $percent% ($free kB) free|$perfdata",$exit_codes{'WARNING'});
      }
      else {
          finish("OK - $percent% ($free kB) free|$perfdata",$exit_codes{'OK'});
      }
    }
    elsif ($opt_u) {
      my $percent    = sprintf "%.1f", ($used / $total * 100);
      if ($percent >= $opt_c) {
          finish("CRITICAL - $percent% ($used kB) used|$perfdata",$exit_codes{'CRITICAL'});
      }
      elsif ($percent >= $opt_w) {
          finish("WARNING - $percent% ($used kB) used|$perfdata",$exit_codes{'WARNING'});
      }
      else {
          finish("OK - $percent% ($used kB) used|$perfdata",$exit_codes{'OK'});
      }
    }
}

# Show usage
sub usage() {
  print "\ncheck_mem.pl v1.0 - Nagios Plugin\n\n";
  print "usage:\n";
  print " check_mem.pl -<f|u> -w <warnlevel> -c <critlevel>\n\n";
  print "options:\n";
  print " -f           Check FREE memory\n";
  print " -u           Check USED memory\n";
  print " -C           Count OS caches as FREE memory\n";
  print " -w PERCENT   Percent free/used when to warn\n";
  print " -c PERCENT   Percent free/used when critical\n";
  print "\nCopyright (C) 2000 Dan Larsson <dl\@tyfon.net>\n";
  print "check_mem.pl comes with absolutely NO WARRANTY either implied or explicit\n";
  print "This program is licensed under the terms of the\n";
  print "MIT License (check source code for details)\n";
  exit $exit_codes{'UNKNOWN'}; 
}

sub get_memory_info {
    my $used_memory_kb  = 0;
    my $free_memory_kb  = 0;
    my $total_memory_kb = 0;
    my $caches_kb       = 0;

    my $uname;
    if ( -e '/usr/bin/uname') {
        $uname = `/usr/bin/uname -a`;
    }
    elsif ( -e '/bin/uname') {
        $uname = `/bin/uname -a`;
    }
    else {
        die "Unable to find uname in /usr/bin or /bin!\n";
    }
    print "uname returns $uname" if ($opt_v);
    if ( $uname =~ /Linux/ ){
        my @meminfo = `/bin/cat /proc/meminfo`;
        foreach (@meminfo) {
            chomp;
            if (/^Mem(Total|Free):\s+(\d+) kB/) {
                my $counter_name = $1;
                if ($counter_name eq 'Free') {
                    $free_memory_kb = $2;
                }
                elsif ($counter_name eq 'Total') {
                    $total_memory_kb = $2;
                }
            }
            elsif (/^(Buffers|Cached|SReclaimable):\s+(\d+) kB/) {
                $caches_kb += $2;
            }
        }
        $used_memory_kb = $total_memory_kb - $free_memory_kb;
    }
	
    elsif (( $uname =~ /CYGWIN/ ) || ( $uname =~ /MINGW/ )){
        #Cygwin or MinGW on Windows
		my @meminfo = `vmstat -s`;
        foreach (@meminfo) {
            chomp;
            if (/^\s+(\d+) K (total|free|used) memory/) {
                my $counter_name = $2;
                if ($counter_name eq 'free') { $free_memory_kb = $1; }
                elsif ($counter_name eq 'total') { $total_memory_kb = $1; }
				elsif ($counter_name eq 'used') { $used_memory_kb = $1; }
            }
        }
    }	

    elsif ( $uname =~ /FreeBSD/ ) {
      # The FreeBSD case. 2013-03-19 www.claudiokuenzler.com
      # free mem = Inactive*Page Size + Cache*Page Size + Free*Page Size
      my $pagesize = `sysctl vm.stats.vm.v_page_size`;
      $pagesize =~ s/[^0-9]//g;
      my $mem_inactive = 0;
      my $mem_cache = 0;
      my $mem_free = 0;
      my $mem_total = 0;
      my $free_memory = 0;
        my @meminfo = `/sbin/sysctl vm.stats.vm`;
        foreach (@meminfo) {
            chomp;
            if (/^vm.stats.vm.v_inactive_count:\s+(\d+)/) {
            $mem_inactive = ($1 * $pagesize);
            }
            elsif (/^vm.stats.vm.v_cache_count:\s+(\d+)/) {
            $mem_cache = ($1 * $pagesize);
            }
            elsif (/^vm.stats.vm.v_free_count:\s+(\d+)/) {
            $mem_free = ($1 * $pagesize);
            }
            elsif (/^vm.stats.vm.v_page_count:\s+(\d+)/) {
            $mem_total = ($1 * $pagesize);
            }
        }
        $free_memory = $mem_inactive + $mem_cache + $mem_free;
        $free_memory_kb = ( $free_memory / 1024);
        $total_memory_kb = ( $mem_total / 1024);
        $used_memory_kb = $total_memory_kb - $free_memory_kb;
        $caches_kb = ($mem_cache / 1024);
    }
    elsif ( $uname =~ /SunOS/ ) {
        eval "use Sun::Solaris::Kstat";
        if ($@) { #Kstat not available
            if ($opt_C) {
                print "You can't report on Solaris caches without Sun::Solaris::Kstat available!\n";
                exit $exit_codes{UNKNOWN};
            }
            my @vmstat = `/usr/bin/vmstat 1 2`;
            my $line;
            foreach (@vmstat) {
              chomp;
              $line = $_;
            }
            $free_memory_kb = (split(/ /,$line))[5] / 1024;
            my @prtconf = `/usr/sbin/prtconf`;
            foreach (@prtconf) {
                if (/^Memory size: (\d+) Megabytes/) {
                    $total_memory_kb = $1 * 1024;
                }
            }
            $used_memory_kb = $total_memory_kb - $free_memory_kb;
            
        }
        else { # We have kstat
            my $kstat = Sun::Solaris::Kstat->new();
            my $phys_pages = ${kstat}->{unix}->{0}->{system_pages}->{physmem};
            my $free_pages = ${kstat}->{unix}->{0}->{system_pages}->{freemem};
            # We probably should account for UFS caching here, but it's unclear
            # to me how to determine UFS's cache size.  There's inode_cache,
            # and maybe the physmem variable in the system_pages module??
            # In the real world, it looks to be so small as not to really matter,
            # so we don't grab it.  If someone can give me code that does this, 
            # I'd be glad to put it in.
            my $arc_size = (exists ${kstat}->{zfs} && ${kstat}->{zfs}->{0}->{arcstats}->{size}) ?
                 ${kstat}->{zfs}->{0}->{arcstats}->{size} / 1024 
                 : 0;
            $caches_kb += $arc_size;
            my $pagesize = `pagesize`;
    
            $total_memory_kb = $phys_pages * $pagesize / 1024;
            $free_memory_kb = $free_pages * $pagesize / 1024;
            $used_memory_kb = $total_memory_kb - $free_memory_kb;
        }
    }
    elsif ( $uname =~ /AIX/ ) {
        my @meminfo = `/usr/bin/vmstat -v`;
        foreach (@meminfo) {
            chomp;
            if (/^\s*([0-9.]+)\s+(.*)/) {
                my $counter_name = $2;
                if ($counter_name eq 'memory pages') {
                    $total_memory_kb = $1*4;
                }
                if ($counter_name eq 'free pages') {
                    $free_memory_kb = $1*4;
                }
                if ($counter_name eq 'file pages') {
                    $caches_kb = $1*4;
                }
            }
        }
        $used_memory_kb = $total_memory_kb - $free_memory_kb;
    }
    else {
        if ($opt_C) {
            print "You can't report on $uname caches!\n";
            exit $exit_codes{UNKNOWN};
        }
    	my $command_line = `vmstat | tail -1 | awk '{print \$4,\$5}'`;
    	chomp $command_line;
        my @memlist      = split(/ /, $command_line);
    
        # Define the calculating scalars
        $used_memory_kb  = $memlist[0]/1024;
        $free_memory_kb = $memlist[1]/1024;
        $total_memory_kb = $used_memory_kb + $free_memory_kb;
    }
    return ($free_memory_kb,$used_memory_kb,$caches_kb);
}

sub init {
    # Get the options
    if ($#ARGV le 0) {
      &usage;
    }
    else {
      getopts('c:fuCvw:');
    }
    
    # Shortcircuit the switches
    if (!$opt_w or $opt_w == 0 or !$opt_c or $opt_c == 0) {
      print "*** You must define WARN and CRITICAL levels!\n";
      &usage;
    }
    elsif (!$opt_f and !$opt_u) {
      print "*** You must select to monitor either USED or FREE memory!\n";
      &usage;
    }
    
    # Check if levels are sane
    if ($opt_w <= $opt_c and $opt_f) {
      print "*** WARN level must not be less than CRITICAL when checking FREE memory!\n";
      &usage;
    }
    elsif ($opt_w >= $opt_c and $opt_u) {
      print "*** WARN level must not be greater than CRITICAL when checking USED memory!\n";
      &usage;
    }
}

sub finish {
    my ($msg,$state) = @_;
    print "$msg\n";
    exit $state;
}
