package AdventREST::Controller::User;

use strict;
use warnings;
use base 'Catalyst::Controller::REST';

# If we have a form, return JSON data
__PACKAGE__->config(
    'serialize' => {
        'default' => 'text/x-json',
        'map' => {
            'text/xml'  => [ 'View', 'TT' ],
            'text/html' => [ 'View', 'TT' ],
            # Remap x-www-form-urlencoded to use JSON for serialization
            'application/x-www-form-urlencoded' => 'JSON',
        },
    }
);

=head1 NAME

AdventREST::Controller::User - Catalyst Advent Calendar REST Controller 

=head1 DESCRIPTION

This is the Catalyst Advent Calendar 2006 REST example controller.  
It shows how to implement a basic RESTful Web Service in Catalyst using
L<Catalyst::Controller::REST>

=head1 METHODS

=cut

=head2 user_list :Path('/user') :ActionClass('REST')

This method sets up the /user url, which lists all of our users.  Requests
will be dispatched to user_list_METHOD.

=cut

sub user_list : Path('/user') :Args(0) ActionClass('REST') {
}

=head2 user_list_GET

Handles GET requests on '/user'.  Returns a hash of users, with URIs for
each one.   Returns a 200 OK with the hash of users.

=cut

sub user_list_GET {
    my ( $self, $c ) = @_;

    $c->log->debug( $c->req->content_type );
    #$c->log->debug( $c->req->headers->as_string );

    my %user_list;
    my $user_rs = $c->model('DB::User')->search;
    while ( my $user_row = $user_rs->next ) {
        $user_list{ $user_row->user_id } =
          $c->uri_for( '/user/' . $user_row->user_id )->as_string;
    }
    $self->status_ok( $c, entity => \%user_list );

    my $page     = $c->req->params->{page} || 1;
    my $per_page = $c->req->params->{per_page} || 10;

    # We'll use an array now:
    my @user_list;
    my $rs = $c->model('DB::User')
        ->search(undef, { rows => $per_page })->page( $page );
    while ( my $user_row = $rs->next ) {
        push @user_list, {
            $user_row->get_columns,
            uri => $c->uri_for( '/user/' . $user_row->user_id )->as_string
        };
    }

    $self->status_ok( $c, entity => {
        result_set => {
            totalResultsAvailable => $rs->pager->total_entries,
            totalResultsReturned  => $rs->pager->entries_on_this_page,
            firstResultPosition   => $rs->pager->current_page,
            result => [ @user_list ]
        }
    });
}

=head2 single_user :Path('/user') :Args(1) :ActionClass('REST')

Sets up requests to '/user/*'.  The only argument is a user_id, which we 
use to perform a quick lookup for the user, storing the data in the stash
for use in later methods.

=cut

sub single_user : Path('/user') :Args(1) : ActionClass('REST') {
    my ( $self, $c, $user_id ) = @_;

    $c->stash->{'user'} = $c->model('DB::User')->find($user_id);
}

=head2 single_user_POST 

Creates or updates a user in the database.   Returns 400 BAD REQUEST if the
data submitted is incomplete, or if an attempt is made to create a resource
at a bad URI.  Returns 200 OK if the user was modified, and 201 CREATED if
it was created.

=cut

sub single_user_POST {
    my ( $self, $c, $user_id ) = @_;

    my $new_user_data = $c->req->data;
    if ( !defined($new_user_data) ) {
        return $self->status_bad_request( $c,
            message => "You must provide a user to create or modify!" );
    }

    if ( $new_user_data->{'user_id'} ne $user_id ) {
        return $self->status_bad_request( $c,
                message => "Cannot create or modify user "
              . $new_user_data->{'user_id'} . " at "
              . $c->req->uri->as_string
              . "; the user_id does not match!" );
    }

    foreach my $required (qw(user_id fullname description)) {
        return $self->status_bad_request( $c,
            message => "Missing required field: " . $required )
          if !exists( $new_user_data->{$required} );
    }
    my $user = $c->model('DB::User')->update_or_create(
        user_id     => $new_user_data->{'user_id'},
        fullname    => $new_user_data->{'fullname'},
        description => $new_user_data->{'description'},
    );
    my $return_entity = {
        user_id     => $user->user_id,
        fullname    => $user->fullname,
        description => $user->description,
        uri         => $c->uri_for( $c->controller->action_for('single_user'), $user->user_id )->as_string
    };

    if ( $c->stash->{'user'} ) {
        $self->status_ok( $c, entity => $return_entity, );
    } else {
        $self->status_created(
            $c,
            location => $c->req->uri->as_string,
            entity   => $return_entity,
        );
    }
}

=head2 single_user_PUT

The same as single_user_POST.  This is often the case in REST, since a PUT request should modify the entry it describes if it already exists.  

=cut

*single_user_PUT = *single_user_POST;

=head2 single_user_GET

Returns a user.

=cut

sub single_user_GET {
    my ( $self, $c, $user_id ) = @_;

    my $user = $c->stash->{'user'};
    if ( defined($user) ) {
        $self->status_ok(
            $c,
            entity => {
                user_id     => $user->user_id,
                fullname    => $user->fullname,
                description => $user->description,
            }
        );
    } else {
        $self->status_not_found( $c,
            message => "Could not find User $user_id!" );
    }
}

=head2 single_user_DELETE

Removes a user from the database.  Returns 200 OK with the deleted entity 
on success, or 404 NOT FOUND if the user doesn't exist. 

=cut

sub single_user_DELETE {
    my ( $self, $c, $user_id ) = @_;

    my $user = $c->stash->{'user'};
    if ( defined($user) ) {
        $user->delete;
        $self->status_ok(
            $c,
            entity => {
                user_id     => $user->user_id,
                fullname    => $user->fullname,
                description => $user->description,
            }
        );
    } else {
        $self->status_not_found( $c,
            message => "Cannot delete non-existent user $user_id!" );
    }
}

=head1 AUTHOR

Adam Jacob L<mailto:adam@stalecoffee.org>

=head1 LICENSE

This library is free software, you can redistribute it and/or modify
it under the same terms as Perl itself.

=cut

1;
