package CloudStenography::Controller::Root;

use strict;
use warnings;
use parent 'Catalyst::Controller';

use Data::Dump qw/dump/;

#
# Sets the actions in this controller to be registered with no prefix
# so they function identically to actions created in MyApp.pm
#
__PACKAGE__->config->{namespace} = '';

=head1 NAME

CloudStenography::Controller::Root - Root Controller for CloudStenography

=head1 DESCRIPTION

[enter your description here]

=head1 METHODS

=cut

=head2 index

=cut

#sub index :Path :Args(0) {
#    my ( $self, $c ) = @_;
#
#    # Hello World
#    $c->response->body( $c->welcome_message );
#    
#    my $foo = $c->model('CloudDB::LanguageWords')->new({name => 'foo', working => 'bar', language => 'pig'});
#    $foo->insert;
#    
#    $c->log->debug(dump($foo));
#}

sub default :Path {
    my ( $self, $c ) = @_;
    $c->response->body( 'Page not found' );
    $c->response->status(404);
}

=head2 end

Attempt to render a view, if needed.

=cut

sub end : ActionClass('RenderView') {}

=head1 AUTHOR

Peyompian

=head1 LICENSE

This library is free software. You can redistribute it and/or modify
it under the same terms as Perl itself.

=cut

1;
