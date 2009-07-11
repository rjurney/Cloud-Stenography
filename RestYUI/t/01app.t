use strict;
use warnings;
use FindBin;
use lib "$FindBin::Bin/../lib";
use Test::More tests => 2;

BEGIN { use_ok 'Catalyst::Test', 'AdventREST' }

ok( request('/')->is_error, 'Request should fail' );
