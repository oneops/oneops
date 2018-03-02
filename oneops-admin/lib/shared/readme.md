# Useful test commands:
## check all available combinations of platforms and suites to test
`kitchen list`

## test every suite one-by-one:
`kitchen test`

## test one suite - full cycle - create-converge-verify-destroy
`kitchen test chef-1140-centos-69`

## test one suite - one step at a time
```
kitchen create chef-1140-centos-69
kitchen converge chef-1140-centos-69
kitchen verify chef-1140-centos-69
kitchen destroy chef-1140-centos-69
```

## SSH to a test VM
`kitchen login chef-1140-centos-69`

# Other comments
- To test with vendor cache, the vendor/cache folder can be copied from any production inductor, or built separately.
If building separately make sure the build machine matches inductor's architecture (platform, os version, ruby version)
- if not using vendor cache, to speed up verify step you might want to alter rubygems.rb/install_using_prebuilt_gemfile method, by removing `--full-index` option
