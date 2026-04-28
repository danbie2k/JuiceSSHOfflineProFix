insert or replace into modules(module_pkg_name,apk_path) values('com.owner.juicesshofflinepro','/data/app/~~QrTajwZhYdgl1LWhQMxL6Q==/com.owner.juicesshofflinepro-fgqFlpywLZbX5tZtr3dPsg==/base.apk');
insert or replace into modules_state(module_pkg_name,user_id,enabled,scope_request_blocked) values('com.owner.juicesshofflinepro',0,1,0);
insert or replace into scope(module_pkg_name,app_pkg_name,user_id) values('com.owner.juicesshofflinepro','com.sonelli.juicessh',0);
select * from modules_state where module_pkg_name='com.owner.juicesshofflinepro';
select * from scope where module_pkg_name='com.owner.juicesshofflinepro';
