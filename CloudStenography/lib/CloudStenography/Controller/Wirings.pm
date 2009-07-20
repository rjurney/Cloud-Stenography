package CloudStenography::Controller::Wirings;

use strict;
use warnings;

use base 'Catalyst::Controller';

use Data::Dump qw/dump/;

# If we have a form, return JSON data
__PACKAGE__->config(
    'serialize' => {
        'default' => 'text/x-json',
        'map' => {
            'text/xml'  => [ 'View', 'TT' ],
            'text/html' => [ 'View', 'TT' ],
            # Remap x-www-form-urlencoded to use JSON for serialization
            'application/json' => 'JSON',
        },
    }
);


=head2 saveWiring

    SMD:
   "saveWiring" : {
      "description": "Save the module",
      "parameters": [
         {"name":"name","type":"string"},
         {"name":"working","type":"text"},
         {"name":"language","type":"text"}
      ]
   },

=cut

sub saveWiring : JSONRPCPath('/saveWiring')
{
    my ( $self, $c, @args ) = @_;
    
    my $name = $c->req->param('name');
    my $working = $c->req->param('working');
    my $language = $c->req->param('language');
    
    # Save if all parameters present, or throw error
    if($name and $working and $language)
    {
        my $script = $c->model('CloudDB::LanguageWords')->new({name => $name, working => $working, language => $language});
        $script->insert;
    }
    else
    {
        $c->log->debug("Problem saving name: $name language: $language working: $working");
        $c->stash->{jsonrpc} = 'error';
        return;
    }
    
    $c->stash->{jsonrpc} = 1;
}


=head2 listWirings

    SMD:
    "listWirings" : {
       "description": "Get the list of modules",
       "parameters": [
          {"name":"language","type":"text"}
       ]
    },

=cut

sub listWirings : JSONRPCPath('/listWirings') 
{

    my ( $self, $c, @args ) = @_;
    
    # We will support more than one eventually
    my $language = ($c->req->param('language') or 'pig');
    
    my @wiring_objs = $c->model('CloudDB::LanguageWords')->search({language => $language});
    
    my @wirings;
    foreach my $wiring (@wiring_objs)
    {
        # Stuff a hash into the array
        push @wirings, {
                            id => $wiring->id,
                            name => $wiring->name,
                            language => $wiring->language,
                            working => $wiring->working,
                        };
    }
    
    $c->stash->{jsonrpc} = \@wirings;
}


=head2 loadWiring

    SMD:
    "loadWiring" : {
       "description": "Load the module",
       "parameters": [
          {"name":"name","type":"string"},
          {"name":"language","type":"text"}
       ]
    },

=cut

sub loadWiring
{
    my ( $self, $c, @args ) = @_;
    
    my $name = $c->req->param('name');
    my $language = $c->req->param('language');
    
    if($name and $language)
    {
        my $wiring = $c->model('CloudDB::LanguageWords')->find({name => $name, language => $language});
        if($wiring)
        {
            $c->stash->{jsonrpc} = $wiring->get_columns;
        }
        else
        {
            $c->log->debug("No such wiring name: $name language: $language");
        }
    }
    else
    {
        $c->log->debug("Must supply wiring name and language");
        $c->stash->{jsonrpc} = 'error';
        return;
    }
}


=head2 deleteWiring

    SMD:
    "deleteWiring" : {
       "description": "Delete the module",
       "parameters": [
          {"name":"name","type":"string"},
          {"name":"language","type":"text"}
       ]
    }

=cut

sub deleteWiring
{
    my ( $self, $c, @args ) = @_;
    
    my $name = $c->req->param('name');
    my $language = $c->req->param('language');
    
    if($name and $language)
    {
        my $wiring = $c->model('CloudDB::LanguageWords')->find({name => $name, language => $language});
        if($wiring)
        {
            $wiring->delete;
        }
        else
        {
            $c->log->debug("No such wiring name: $name language: $language");
        }
    }
    else
    {
        $c->log->debug("Must supply wiring name and language");
        $c->stash->{jsonrpc} = 'error';
        return;
    }
}

=head2 runWiring

    SMD:
   "runWiring" : {
      "description": "Run the module",
      "parameters": [
         {"name":"name","type":"string"},
         {"name":"working","type":"text"},
         {"name":"language","type":"text"}
      ]
   },

=cut

sub runWiring
{
    my ( $self, $c, @args ) = @_;
    
    my $name = $c->req->param('name');
    my $language = $c->req->param('language');
    my $working = $c->req->param('working');
    
    my $foo = system("pwd");

    $c->log->debug("PWD: " . $foo);
}

=head2 illustrateWiring

    SMD:
   "illustrateWiring" : {
      "description": "Illustrate the module",
      "parameters": [
         {"name":"name","type":"string"},
         {"name":"working","type":"text"},
         {"name":"language","type":"text"}
      ]
   },

=cut

sub illustrateWiring
{
    my ( $self, $c, @args ) = @_;
    
    my $name = $c->req->param('name');
    my $language = $c->req->param('language');
    my $working = $c->req->param('working');
    
    my $foo = system("pwd");

    $c->log->debug("PWD: " . $foo);
}

1;
