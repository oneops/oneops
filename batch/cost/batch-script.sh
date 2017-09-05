pr=`env | grep ONEOPS_CLOUD_ADMINSTATUS | awk '{split($0,a,"="); print a[2]}' | tr [a-z] [A-Z]`

if [ "$pr" == "PRIMARY" ]; then
    en=`env | grep ONEOPS_ENVIRONMENT | awk '{split($0,a,"="); print a[2]}' | tr [a-z] [A-Z]`
    n=0
    until [ $n -ge 5 ]
    do
        cd /opt/oneops/cost;./cost-batch-job.rb '$OO_LOCAL{es-host}' `date +%Y-%m-%d --date='-7 day'` `date +%Y-%m-%d --date='-1 day'` 2>&1 >>/opt/oneops/log/cost_batch_status.log && break
        n=$[$n+1]
        mail -s "$en :: daily cost indexer failed for `date +%Y-%m-%d --date='-1 day'` at `date`. Retry attempt $n " OneOpsSprt@email.wal-mart.com
        sleep 60m
    done
fi
