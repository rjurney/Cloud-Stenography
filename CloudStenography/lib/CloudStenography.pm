package CloudStenography;

use strict;
use warnings;

use Catalyst::Runtime 5.80;

# Set flags and add plugins for the application
#
#         -Debug: activates the debug mode for very useful log messages
#   ConfigLoader: will load the configuration from a Config::General file in the
#                 application's home directory
# Static::Simple: will serve static files from the application's root
#                 directory

use parent qw/Catalyst/;
use Catalyst qw/-Debug
                ConfigLoader
                Static::Simple
                Server
                Server::JSONRPC/;
our $VERSION = '0.01';

# Configure the application.
#
# Note that settings in cloudstenography.conf (or other external
# configuration file that you set up manually) take precedence
# over this when using ConfigLoader. Thus configuration
# details given here can function as a default configuration,
# with an external configuration file acting as an override for
# local deployment.

__PACKAGE__->config(
                        name => 'CloudStenography',
                        jsonrpc => {
                            path => qr#^(/?)wirings(/|$)#,
                        }
                    );

__PACKAGE__->config->{static}->{ignore_extensions} = [
    qw/tmpl tt tt2 xhtml/ 
 ];
 
 #__PACKAGE__->config(
 #   # Set the default serialization to JSON
 #   'default' => 'JSON',
 #   'map' => {
 #       # Remap x-www-form-urlencoded to use JSON for serialization
 #       'application/json' => 'JSON',
 #   },
 #);

# Start the application
__PACKAGE__->setup();

#Moose::Util::apply_all_roles(CloudStenography->log, 'Catalyst::TraitFor::Log::Audio');


=head1 NAME

CloudStenography - Catalyst based application

=head1 SYNOPSIS

    script/cloudstenography_server.pl

=head1 DESCRIPTION

[enter your description here]

=head1 SEE ALSO

L<CloudStenography::Controller::Root>, L<Catalyst>

=head1 AUTHOR

Peyompian

=head1 LICENSE

This library is free software. You can redistribute it and/or modify
it under the same terms as Perl itself.

=cut

1;
