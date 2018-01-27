#!/usr/bin/env ruby
require 'json'

@curl_get_command='curl -s -X GET -H "Content-Type: application/json" -H "Accept: application/json" -u test:test123'
@curl_post_command='curl -s -X POST -H "Content-Type: application/json" -H "Accept: application/json" -u test:test123'
@curl_put_command='curl -s -X PUT -H "Content-Type: application/json" -H "Accept: application/json" -u test:test123'
@curl_delete_command='curl -s -X DELETE -H "Content-Type: application/json" -H "Accept: application/json" -u test:test123'

@stub_params="-Dstub.clouds=openstack -DstubResultCode=0 -Dstub.responseTimeInSeconds=1"
@original_params=File.read("/opt/oneops/inductor/clouds-available/shared/conf/vmargs")
@new_params = @original_params.gsub("\n", "") << " #{@stub_params}\n"

def check_for_failure(response, request)
	if response.nil? || !$?.success?
		request == "disabling environment" ? (abort "****** failing while #{request} #{response} so aborting ******") : (abort "****** failing while #{request} #{response["errors"]} #{response} so aborting ******")
	else
		request == "disabling environment" ? (puts "****** success while #{request} ******") : (puts "****** success while #{request} #{response["errors"]} ******")
	end
end

def creating_user()
	response=JSON.parse(`#{@curl_post_command.gsub(" -u test:test123", "")} -d '{ "user": { "email": "test@oneops.com", "username": "test", "password": "test123", "password_confirmation": "test123", "name": "test" } }' http://localhost:3000/users 2>&1`)
	check_for_failure(response, "creating user")
end

def accept_user_terms_conditions()
	`#{@curl_get_command} http://localhost:3000/users/sign_in`
	`#{@curl_put_command} http://localhost:9090/account/profile/accept_eula -d '{ "eula_accepted":"true" }' http://localhost:3000/account/profile/accept_eula`
end

def creating_organization()
	response=JSON.parse(`#{@curl_post_command} -d '{ "name": "test" }' http://localhost:3000/account/organizations 2>&1`)
	check_for_failure(response, "creating organization")
end

def creating_assembly()
	response=JSON.parse(`#{@curl_post_command} -d '{ "cms_ci": { "ciName": "validation", "ciAttributes": { "owner": "test@test.com" } } }' http://localhost:3000/test/assemblies 2>&1`)
	check_for_failure(response, "creating assembly")
end

def creating_platform()
	response=JSON.parse(`#{@curl_post_command} -d '{ "cms_dj_ci": { "comments": "creating platform for OO validation", "ciName": "tomcat", "ciAttributes": { "source": "oneops", "description": "tomcat platform", "pack": "tomcat", "version": "1" } } }' http://localhost:3000/test/assemblies/validation/design/platforms 2>&1`)
	check_for_failure(response, "creating platform")
end

def committing_design()
	design_release=JSON.parse(`#{@curl_get_command} http://localhost:3000/test/assemblies/validation/design/releases/latest 2>&1`)
	check_for_failure(design_release, "getting design release")
	design_release_id=design_release["releaseId"]
	response=JSON.parse(`#{@curl_post_command} -d '' http://localhost:3000/test/assemblies/validation/design/releases/#{design_release_id}/commit 2>&1`)
	check_for_failure(response, "committing design")
end

def creating_cloud()
	response=JSON.parse(`#{@curl_post_command} -d '{ "cms_ci" : { "ciName" : "openstack", "ciAttributes" : { "description" : "cloud for OO validation", "location" : "/public/oneops/clouds/openstack" } } }' http://localhost:3000/test/clouds 2>&1`)
	check_for_failure(response, "creating cloud")
end

def creating_cloud_service(name, identifier)
	cloud_services=JSON.parse(`#{@curl_get_command} http://localhost:3000/test/clouds/openstack/services/available.json 2>&1`)
	check_for_failure(cloud_services, "getting cloud services")
	service_details=cloud_services["#{name}"].select { |c| c["ciName"]=="#{identifier}" }
	service_id=service_details[0]["ciId"]

	service_data=JSON.parse(`#{@curl_get_command} -d '{ "mgmtCiId": "#{service_id}" }' http://localhost:3000/test/clouds/openstack/services/new.json 2>&1`)
	check_for_failure(service_data, "getting cloud #{name} #{identifier} service data")
	service_data["ciAttributes"]["tenant"], service_data["ciAttributes"]["username"], service_data["ciAttributes"]["password"]="test", "test", "test" if name=="compute" || name=="dns" || name=="lb"
	service_data["ciAttributes"]["domain_owner_email"]="test@test.com" if name=="dns"
	service_data=service_data.to_json
	response=JSON.parse(`#{@curl_post_command} -d '{ "mgmtCiId": "#{service_id}", "cms_ci": #{service_data} }' http://localhost:3000/test/clouds/openstack/services 2>&1`)
	check_for_failure(response, "creating cloud #{name} #{identifier} service")
end

def creating_environment()
	cloud=JSON.parse(`#{@curl_get_command} http://localhost:3000/test/clouds/openstack 2>&1`)
	check_for_failure(cloud, "getting cloud")
	cloud_cid=cloud["ciId"]
	platform=JSON.parse(`#{@curl_get_command} http://localhost:3000/test/assemblies/validation/design/platforms/tomcat 2>&1`)
	check_for_failure(platform, "getting platform")
	platform_cid=platform["ciId"]
	response=JSON.parse(`#{@curl_post_command} -d '{ "clouds": { "#{cloud_cid}":"1" }, "platform_availability": { "#{platform_cid}": "redundant" }, "cms_ci": { "ciName": "test", "nsPath": "test/validation", "ciAttributes": { "autorepair": "false", "monitoring": "true", "description": "OO validation environment", "dpmtdelay": "60", "subdomain": "test.validation.test", "codpmt": "false", "debug": "false", "global_dns": "true", "autoscale": "true", "availability": "redundant" } } }' http://localhost:3000/test/assemblies/validation/transition/environments 2>&1`)
	check_for_failure(response, "creating environment")
end

def committing_environment()
	response=JSON.parse(`#{@curl_post_command} -d '' http://localhost:3000/test/assemblies/validation/transition/environments/test/commit 2>&1`)
	check_for_failure(response, "committing environment")
	sleep(5)
end

def restart_inductor()
	`su -l ooadmin -c "cd /opt/oneops/inductor && inductor restart"`
end

def enabling_stub_inductor()
	puts "****** enabling stub inductor ******"
	File.write("/opt/oneops/inductor/clouds-available/shared/conf/vmargs", "#{@new_params}")
	restart_inductor
end

def deploying_environment()
	environment_release=JSON.parse(`#{@curl_get_command} http://localhost:3000/test/assemblies/validation/transition/environments/test/releases/bom 2>&1`)
	check_for_failure(environment_release, "getting environment release")
	environment_release_id=environment_release["releaseId"]
	deployment=JSON.parse(`#{@curl_post_command} -d '{ "cms_deployment": { "releaseId": "#{environment_release_id}", "nsPath": "/test/validation/test/bom" } }' http://localhost:3000/test/assemblies/validation/transition/environments/test/deployments 2>&1`)
	check_for_failure(deployment, "deploying environment")
end

def validate_deployment_status()
	retry_count=1
	while  retry_count<=3
		response=JSON.parse(`#{@curl_get_command} http://localhost:3000/test/assemblies/validation/transition/environments/test/deployments/latest 2>&1`)
		check_for_failure(response, "getting environment deployment")
		if response["deploymentState"] == "complete"
			puts "****** deployment state is #{response["deploymentState"]} now ******"
			break
		else
			puts "****** deployment state is #{response["deploymentState"]} .. sleeping 15 seconds to see if it gets completed ******"
			abort "****** failing while validating deployment status so aborting ******" if retry_count == 3
			sleep 15
		end
		retry_count+=1
	end
end

def disabling_environment()
	response=JSON.parse(`#{@curl_put_command} -d '' http://localhost:3000/test/assemblies/validation/transition/environments/test/disable 2>&1`)
	check_for_failure(response, "disabling environment")
end

def deleting_environment()
	response=JSON.parse(`#{@curl_delete_command} http://localhost:3000/test/assemblies/validation/transition/environments/test 2>&1`)
	check_for_failure(response, "deleting environment")
end

def deleting_platform()
	response=JSON.parse(`#{@curl_delete_command} http://localhost:3000/test/assemblies/validation/design/platforms/tomcat 2>&1`)
	check_for_failure(response, "deleting platform")
end

def deleting_assembly()
	response=JSON.parse(`#{@curl_delete_command} http://localhost:3000/test/assemblies/validation 2>&1`)
	check_for_failure(response, "deleting assembly")
end

def deleting_organization()
	response=JSON.parse(`#{@curl_delete_command} http://localhost:3000/account/organizations/test 2>&1`)
	check_for_failure(response, "deleting organization")
end

def disabling_stub_inductor()
	puts "****** disabling stub inductor ******"
	File.write("/opt/oneops/inductor/clouds-available/shared/conf/vmargs", @original_params.gsub(@stub_params, ""))
	restart_inductor
end

# creating user test:test123
creating_user

# accepting new terms and conditions
accept_user_terms_conditions

# creating organization test
creating_organization

# creating assembly validation
creating_assembly

# creating platform tomcat
creating_platform

# committing design
committing_design

# creating cloud openstack
creating_cloud

# creating cloud servies compute, mirror, lb and dns
creating_cloud_service("compute", "nova")
creating_cloud_service("mirror", "mirrors-public")
creating_cloud_service("lb", "neutron")
creating_cloud_service("dns", "designate")

# creating environment
creating_environment

# committing environment
committing_environment

# enabling stub inductor
enabling_stub_inductor unless @original_params.include?(@stub_params)

# deploying environment
deploying_environment

# validating deployment to check if the deployment gets completed
validate_deployment_status

# disabling environment
disabling_environment

# committing environment
committing_environment

# deploying environment
deploying_environment

# validating deployment to check if the deployment gets completed
validate_deployment_status

# deleting environment
deleting_environment

# deleting platform tomcat
deleting_platform

# committing design
committing_design

# deleting assembly validation
deleting_assembly

# deleting organization test
deleting_organization

# disabling stub inductor
disabling_stub_inductor
