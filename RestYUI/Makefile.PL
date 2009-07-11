use inc::Module::Install 0.87;

name 'AdventREST';
all_from 'lib/AdventREST.pm';

requires 'Catalyst' => '5.7003';
requires 'Catalyst::Plugin::ConfigLoader';
requires 'Catalyst::Plugin::Static::Simple';
requires 'Catalyst::Action::REST', "0.31";
requires 'Catalyst::Model::DBIC::Schema';
requires 'DBD::SQLite';
requires 'YAML::Syck'; 
catalyst;

install_script glob('script/*.pl');
auto_install;
resources repository => 'http://dev.catalyst.perl.org/repos/Catalyst/trunk/examples/RestYUI/';

WriteAll;
