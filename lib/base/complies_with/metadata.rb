name             'CompliesWith'
description      'Links cloud to compliance'
long_description IO.read(File.join(File.dirname(__FILE__), 'README.md'))
version          '0.1'
maintainer       'OneOps'
maintainer_email 'support@oneops.com'
license          'Copyright OneOps, All rights reserved.'

grouping 'default',
         :relation => true,
         :packages => %w(base)
