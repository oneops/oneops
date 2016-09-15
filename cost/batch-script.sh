en=`env | grep ONEOPS_ENVIRONMENT | awk '{split($0,a,"="); print a[2]}' | tr [a-z] [A-Z]`
n=0
   until [ $n -ge 5 ]
   do
      ./cost-script.rb $OO_LOCAL{ES_HOST} `date +%Y-%m-%d --date='-1 day'` `date +%Y-%m-%d --date='-1 day'` 2>&1 >>/opt/oneops/log/cost_batch_status.log && break
      n=$[$n+1]
      mail -s "$en :: daily cost indexer failed for `date +%Y-%m-%d --date='-1 day'` at `date`. Retry attempt $n " rajanand@walmartlabs.com
      sleep 15
   done