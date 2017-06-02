#!/usr/bin/env perl
use strict;
use warnings;
use Data::Dumper;
use Net::Stomp;
my $stomp = Net::Stomp->new( { hostname => 's09', port => '61613' } );
$stomp->connect( { login => 'a', passcode => 'a' } );

my $body = '{"rfcCi":
{"ciAttributes":{"port":"8080","ssl_port":"8443",
"java_options":"-Xmx128M -Djava.awt.headless\u003dtrue",
"server_port":"8005","ajp_port":"8009","use_security_manager":"false","version":"6.0.32"},
"ciBaseAttributes":{},"rfcId":1058,"releaseId":1048,"ciId":1515,"nsPath":"/Company/t1/e1/bom",
"ciClassName":"bom.Tomcat","ciName":"p1-Tomcat-1309542219-1431-1","ciGoid":"1511-1147-1515",
"rfcAction":"add","execOrder":2,"comments":"","isActiveInRelease":true,"created":"Jul 1, 2011 11:44:42 AM"},
"proxy":[{"ciAttributes":{"minmemory":"512","maxcpu":"32","ostype":"linux26","mincpu":"1","maxmemory":"4096"},"ciBaseAttributes":{},"rfcId":1049,"releaseId":1048,"ciId":1512,"nsPath":"/Company/t1/e1/bom","ciClassName":"bom.Compute","ciName":"p1-Compute-1309542219-1431-1","ciGoid":"1511-1029-1512","rfcAction":"add","execOrder":1,"comments":"","isActiveInRelease":true,"created":"Jul 1, 2011 11:44:42 AM"}],"zone":{"ciAttributes":{"region":"us-east","endpoint":"https://ec2.us-east-1.amazonaws.com"},"ciId":1389,"ciName":"ec2.us-east-1a","ciClassName":"account.provider.Zone","nsPath":"/","ciGoid":"100-1154-1389","comments":"","ciState":"default","lastAppliedRfcId":0,"created":"Jul 1, 2011 11:32:54 AM","updated":"Jul 1, 2011 11:32:54 AM"},"token":{"ciAttributes":{"secret":"H4SS5ycukngagRs4nGEPWaMBrR3grhIe7WikCgoS","key":"AKIAJLLTKVXIVLDAYLJA"},"ciId":1426,"ciName":"aws-ec2 Token","ciClassName":"account.provider.ec2.Token","nsPath":"/Company","ciGoid":"1425-1132-1426","ciState":"default","lastAppliedRfcId":0,"created":"Jul 1, 2011 11:42:52 AM","updated":"Jul 1, 2011 11:42:52 AM"},"dpmtRecordId":1100,"deploymentId":1098,"rfcId":1058,"dpmtRecordState":"pending","created":"Jul 5, 2011 4:51:09 PM"}';

	$stomp->send_transactional(
          { destination => '/queue/ec2.us-east-1a.controller.workorders', body => $body, persistent => 'true' }
      ) or print "Couldn't send the message!\n";

print Dumper($@);

