use strict;
use warnings;
use Test::More tests => 3;

BEGIN { use_ok 'Catalyst::Test', 'CloudStenography' }
BEGIN { use_ok 'CloudStenography::Controller::SMD' }

ok( request('/smd')->is_success, 'Request should succeed' );


