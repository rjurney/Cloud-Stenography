#!/usr/bin/perl -w

BEGIN { $ENV{CATALYST_ENGINE} ||= 'FastCGI' }

use strict;
use warnings;
use Getopt::Long;
use Pod::Usage;
use FindBin;
use lib "$FindBin::Bin/../lib";
use AdventREST;

my $help = 0;
my ( $listen, $nproc, $pidfile, $manager, $detach );
 
GetOptions(
    'help|?'      => \$help,
    'listen|l=s'  => \$listen,
    'nproc|n=i'   => \$nproc,
    'pidfile|p=s' => \$pidfile,
    'manager|M=s' => \$manager,
    'daemon|d'    => \$detach,
);

pod2usage(1) if $help;

AdventREST->run( 
    $listen, 
    {   nproc   => $nproc,
        pidfile => $pidfile, 
        manager => $manager,
        detach  => $detach,
    }
);

1;

=head1 NAME

adventrest_fastcgi.pl - Catalyst FastCGI

=head1 SYNOPSIS

adventrest_fastcgi.pl [options]
 
 Options:
   -? -help      display this help and exits
   -l -listen    Socket path to listen on
                 (defaults to standard input)
                 can be HOST:PORT, :PORT or a
                 filesystem path
   -n -nproc     specify number of processes to keep
                 to serve requests (defaults to 1,
                 requires -listen)
   -p -pidfile   specify filename for pid file
                 (requires -listen)
   -d -daemon    daemonize (requires -listen)
   -M -manager   specify alternate process manager
                 (FCGI::ProcManager sub-class)
                 or empty string to disable

=head1 DESCRIPTION

Run a Catalyst application as fastcgi.

=head1 AUTHOR

Sebastian Riedel, C<sri@oook.de>
Maintained by the Catalyst Core Team.

=head1 COPYRIGHT

This library is free software, you can redistribute it and/or modify
it under the same terms as Perl itself.

=cut
