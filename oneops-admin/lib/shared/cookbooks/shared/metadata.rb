name             "Shared"
description      "Shared"
version          "0.0.1"
maintainer       "OneOps"
maintainer_email "support@oneops.com"
license          "Copyright OneOps, All rights reserved."

grouping 'default',
         :access => "global",
         :packages => [ 'base', 'catalog', 'manifest' ]