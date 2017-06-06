#!/usr/bin/env perl
use strict;
use warnings;
use Data::Dumper;
use Net::Stomp;
my $stomp = Net::Stomp->new( { hostname => 's09', port => '61613' } );
$stomp->connect( { login => 'a', passcode => 'a' } );

my $body = '{"rfcCi":{"ciAttributes":{"minmemory":"512","ostype":"linux26","mincpu":"1","maxcpu":"32","maxmemory":"4096"},"ciBaseAttributes":{},"rfcId":1501,"releaseId":1500,"ciId":1861,"nsPath":"/Company/t1/e1/bom","ciClassName":"bom.Compute","ciName":"p2-Compute-1309924118-1431-1","ciGoid":"1511-1029-1861","rfcAction":"add","execOrder":1,"comments":"","isActiveInRelease":true,"created":"Jul 7, 2011 1:08:31 AM"},"zone":{"ciAttributes":{"region":"us-east","endpoint":"https://ec2.us-east-1.amazonaws.com"},"ciId":1389,"ciName":"ec2.us-east-1a","ciClassName":"account.provider.Zone","nsPath":"/","ciGoid":"100-1154-1389","comments":"","ciState":"default","lastAppliedRfcId":0,"created":"Jul 1, 2011 11:32:54 AM","updated":"Jul 1, 2011 11:32:54 AM"},"token":{"ciAttributes":{"secret":"H4SS5ycukngagRs4nGEPWaMBrR3grhIe7WikCgoS","key":"AKIAJLLTKVXIVLDAYLJA"},"ciId":1426,"ciName":"aws-ec2 Token","ciClassName":"account.provider.ec2.Token","nsPath":"/Company","ciGoid":"1425-1132-1426","ciState":"default","lastAppliedRfcId":0,"created":"Jul 1, 2011 11:42:52 AM","updated":"Jul 1, 2011 11:42:52 AM"},"dpmtRecordId":1569,"deploymentId":1568,"rfcId":1501,"dpmtRecordState":"pending","created":"Jul 7, 2011 2:26:37 AM"}';

	$stomp->send_transactional(
          { destination => '/queue/ec2.us-east-1a.controller.workorders', body => $body, persistent => 'true' }
      ) or print "Couldn't send the message!\n";

print Dumper($@);

