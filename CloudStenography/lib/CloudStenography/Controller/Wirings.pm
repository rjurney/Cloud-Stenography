package CloudStenography::Controller::Editor;

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



# SMD:
#"saveWiring" : {
#   "description": "Save the module",
#   "parameters": [
#      {"name":"name","type":"string"},
#      {"name":"working","type":"text"},
#      {"name":"language","type":"text"}
#   ]
#},
sub saveWiring
{
    
}
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