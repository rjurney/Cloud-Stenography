package AdventREST;

use strict;
use warnings;

use Catalyst::Runtime '5.70';

# Set flags and add plugins for the application
#
#         -Debug: activates the debug mode for very useful log messages
#   ConfigLoader: will load the configuration from a YAML file in the
#                 application's home directory
# Static::Simple: will serve static files from the application's root 
#                 directory

use Catalyst qw/ConfigLoader Static::Simple/;

our $VERSION = '0.01';

# Configure the application. 
#
# Note that settings in AdventREST.yml (or other external
# configuration file that you set up manually) take precedence
# over this when using ConfigLoader. Thus configuration
# details given here can function as a default configuration,
# with a external configuration file acting as an override for
# local deployment.

__PACKAGE__->config( 
    name => 'AdventREST' 
);

# Start the application
__PACKAGE__->setup;


=head1 NAME

AdventREST - Catalyst::Action::REST Example for Advent 2006 

=head1 SYNOPSIS

    script/adventrest_server.pl

=head1 DESCRIPTION

This is the sample Web Service to go along with the Catalyst Advent entry on
L<Catalyst::Action::REST>.  You probably want to see
L<AdventREST::Controller::User> for the real meat.

=head1 SEE ALSO

L<AdventREST::Controller::User>, L<Catalyst>

=head1 AUTHOR

Adam Jacob L<mailto:adam@stalecoffee.org>

=head1 LICENSE

This library is free software, you can redistribute it and/or modify
it under the same terms as Perl itself.

=cut

1;
