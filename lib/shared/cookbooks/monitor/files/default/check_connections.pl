#!/usr/bin/perl -w

use strict;
use warnings;
use Getopt::Long;

my $netstat = "/bin/netstat";

my $warning = 0;
my $critical = 0;

my %status = ( 'OK' => 0, 'WARNING' => 1, 'CRITICAL' => 2, 'UNKNOWN' => 3 );
my $exit_status = $status{UNKNOWN};

my %connections =  ( 'tcp' => 0, 'udp' => 0, 'all' => 0 );

GetOptions( 
    "w|warning=s" => \$warning, 
    "c|critical=s" => \$critical
);

usage() if ( $warning !~ /^\d+$/ || $critical !~ /^\d+$/ || $warning == 0 || $critical == 0 || $critical < $warning );

open ( CONNS, "$netstat -ntu |" ) or die ( "Can't get netstat: $!" );
while ( <CONNS> ) {
    chomp();
    next if ( $_ !~ /^tcp|^udp/ );
    $connections{tcp}++ if ( $_ =~ /^tcp/ );
    $connections{udp}++ if ( $_ =~ /^udp/ );
    $connections{all}++;
}
close( CONNS );

my $output = "TCP Conns: $connections{tcp}, UDP Conns: $connections{udp}, ";
if ( $connections{all} > $critical ) {
    $exit_status = $status{CRITICAL};
    $output .= "'CRITICAL'";
} elsif ( $connections{all} > $warning ) {
    $exit_status = $status{WARNING};
    $output .= "'WARNING'";
} else {
    $output .= "'OK'";
}
$output .= " number of connections|tcp=$connections{tcp};$warning;$critical udp=$connections{udp};$warning;$critical total=$connections{all};$warning;$critical";
print "$output\n";

sub usage {
    print "Usage: $0 -w <warning> -c <critical>\n";
    exit $status{UNKNOWN};
}

exit $exit_status;

