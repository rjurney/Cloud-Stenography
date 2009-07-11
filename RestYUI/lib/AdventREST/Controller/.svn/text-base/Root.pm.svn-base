package AdventREST::Controller::Root;

use strict;
use warnings;
use base 'Catalyst::Controller';

#
# Sets the actions in this controller to be registered with no prefix
# so they function identically to actions created in MyApp.pm
#
__PACKAGE__->config->{namespace} = '';

=head1 NAME

AdventREST::Controller::Root - Root Controller for AdventREST

=head1 DESCRIPTION

Simply returns 404.

=head1 METHODS

=cut

=head2 default

Returns 404, and directs you to /users.

=cut

sub default : Private {
    my ( $self, $c ) = @_;

    $c->response->status(404);
    $c->response->content_type('text/plain');
    $c->response->body("Try going to " . $c->uri_for('index'));
}

=head2 index

Displays a TT page for bootstraping the YUI DataTable components

=cut

sub index : Private {
    my ( $self, $c ) = @_;
    $c->forward( $c->view('TT') );
}

=head1 AUTHOR

Adam Jacob

=head1 LICENSE

This library is free software, you can redistribute it and/or modify
it under the same terms as Perl itself.

=cut

1;
