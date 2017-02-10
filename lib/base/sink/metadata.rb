name              'Sink'
description       'Notification sink'
long_description  IO.read(File.join(File.dirname(__FILE__), 'README.md'))
version           '0.1'
maintainer        'OneOps'
maintainer_email  'support@oneops.com'
license           'Copyright OneOps, All rights reserved.'

grouping 'default',
         :access => 'global',
         :packages => ['base']

grouping 'sns',
         :access => 'global',
         :packages => ['account.notification.sns']

grouping 'url',
         :access => 'global',
         :packages => ['account.notification.url']

grouping 'jabber',
         :access => 'global',
         :packages => ['account.notification.jabber']

grouping 'slack',
         :access => 'global',
         :packages => ['account.notification.slack']


attribute 'description',
          :description => 'Description',
          :default => "",
          :format => {
              :help => 'Enter description for this notification sink',
              :category => '1.Global',
              :order => 1
          }

# slack attributes
attribute 'channels',
          :grouping => 'slack',
          :description => 'Channels',
          :required => 'required',
          :data_type => 'Array',
          :default => '[]',
          :format => {
              :help => "List of channel/group to send message to. Use '<Team>/<Channel>' format.",
              :category => '1.Slack Config',
              :pattern => '^\S+\/\S+$',
              :order => 1
          }

attribute 'text_formats',
          :grouping => 'slack',
          :description => 'Text Formats',
          :data_type => 'hash',
          :default => '{}',
          :format => {
              :help => "Formats the message text if it matches the 'pattern|level'. Eg: ':spike|critical' => ':fire: ${text}' will prepend the text with fire emoji for all critical messages containing ':spike'.",
              :category => '1.Slack Config',
              :order => 2
          }

attribute 'notification_fields',
          :grouping => 'slack',
          :description => 'Include Notification Fields',
          :default => 'false',
          :format => {
              :help => 'Enable to include all notification fields in the message.',
              :category => '1.Slack Config',
              :form => {'field' => 'checkbox'},
              :order => 3
          }

# sns attributes 
attribute 'access',
          :grouping => 'sns',
          :description => 'Access Key',
          :required => 'required',
          :format => {
              :help => 'Amazon AWS account access key',
              :category => '2.Credentials',
              :order => 1
          }

attribute 'secret',
          :grouping => 'sns',
          :description => 'Secret Key',
          :required => 'required',
          :encrypted => true,
          :format => {
              :help => 'Amazon AWS account secret key',
              :category => '2.Credentials',
              :order => 2
          }

attribute 'endpoint',
          :grouping => 'sns',
          :description => 'Endpoint',
          :required => 'required',
          :format => {
              :help => 'SNS endpoint URL',
              :category => '3.Settings',
              :order => 1
          }

# Http url attributes
attribute 'service_url',
          :grouping => 'url',
          :description => 'Endpoint URL',
          :required => 'required',
          :format => {
              :help => 'Http(s) Service Endpoint URL',
              :category => '1.Endpoint',
              :order => 1
          }

attribute 'user',
          :grouping => 'url',
          :description => 'Service Username',
          :format => {
              :help => 'Service Username',
              :category => '2.Credentials',
              :order => 2
          }

attribute 'password',
          :grouping => 'url',
          :description => 'Service Password',
          :encrypted => true,
          :format => {
              :help => 'Service Password',
              :category => '2.Credentials',
              :order => 3
          }

# jabber attributes
attribute 'chat_server',
          :grouping => 'jabber',
          :description => 'Chat Server',
          :required => 'required',
          :default => 'chat.walmart.com',
          :format => {
              :help => 'Chat Service Server',
              :category => '2.Settings',
              :order => 1
          }
attribute 'chat_port',
          :grouping => 'jabber',
          :description => 'Chat Server Port',
          :required => 'required',
          :default => '5222',
          :format => {
              :help => 'Port Chat Service Server listens',
              :category => '2.Settings',
              :pattern => '[0-9]+',
              :order => 2
          }
attribute 'chat_room',
          :grouping => 'jabber',
          :description => 'Chat Room Name',
          :required => 'required',
          :default => 'oneops',
          :format => {
              :help => 'Chat Room Name',
              :category => '2.Settings',
              :order => 3
          }
attribute 'chat_conference',
          :grouping => 'jabber',
          :description => 'Conferences Identifier',
          :required => 'required',
          :default => 'conference.chat.walmart.com',
          :format => {
              :help => 'Name configured in chat server for collection of rooms.',
              :category => '2.Settings',
              :order => 4
          }
attribute 'chat_user',
          :grouping => 'jabber',
          :description => 'User Account',
          :required => 'required',
          :default => 'portaltestuser1',
          :format => {
              :help => 'XMPP account that can login, and send chat',
              :category => '2.Settings',
              :order => 5
          }
attribute 'chat_password',
          :grouping => 'jabber',
          :description => 'User Password',
          :encrypted => true,
          :format => {
              :help => 'Password to authenticate the XMPP user',
              :category => '2.Settings',
              :order => 6
          }


# Message Filter Attributes
attribute 'filter_enabled',
          :description => 'Enable Filter',
          :default => 'false',
          :format => {
              :help => 'Disable / Enable notification event filtering',
              :category => '3.Filtering',
              :form => {'field' => 'checkbox'},
              :order => 1
          }

attribute 'event_type',
          :description => 'Event Type',
          :required => 'required',
          :default => 'All',
          :format => {
              :help => 'OneOps notification event type',
              :category => '3.Filtering',
              :filter => {'all' => {'visible' => 'filter_enabled:eq:true'}},
              :order => 2,
              :form => {'field' => 'select', 'options_for_select' => [
                  ['CI', 'ci'],
                  ['Deployment', 'deployment'],
                  ['Procedure', 'procedure'],
                  ['All', 'none']]}
          }

attribute 'severity_level',
          :description => 'Severity Level',
          :required => 'required',
          :default => 'critical',
          :format => {
              :help => 'OneOps notification event severity level',
              :category => '3.Filtering',
              :filter => {'all' => {'visible' => 'filter_enabled:eq:true'}},
              :order => 3,
              :form => {'field' => 'select', 'options_for_select' => [
                  ['Critical', 'critical'],
                  ['Warning', 'warning'],
                  ['Info', 'info'],
                  ['All', 'none']]}
          }

attribute 'env_profile',
          :description => 'Environment Profile Pattern',
          :default => '',
          :format => {
              :help => 'Send notifications only for matching environment profile regex. Ex: Prod|prod|stage. Leave empty to send for all environment profiles',
              :category => '3.Filtering',
              :order => 4
          }

attribute 'ns_paths',
          :description => 'NS Paths',
          :data_type => 'Array',
          :default => '[]',
          :format => {
              :help => 'NS paths to be used for message filtering',
              :filter => {'all' => {'visible' => 'filter_enabled:eq:true'}},
              :category => '3.Filtering',
              :order => 5
          }

attribute 'monitoring_clouds',
          :description => 'Monitoring Clouds',
          :data_type => 'Array',
          :default => '[]',
          :format => {
              :help => 'OneOps clouds to be used for message filtering',
              :filter => {'all' => {'visible' => 'filter_enabled:eq:true'}},
              :category => '3.Filtering',
              :order => 6
          }

attribute 'msg_selector_regex',
          :description => 'Message Pattern',
          :default => '*',
          :format => {
              :help => 'Selector regex to filter messages. * to select all the messages.',
              :filter => {'all' => {'visible' => 'filter_enabled:eq:true'}},
              :category => '3.Filtering',
              :order => 7
          }

# Message Transformation
attribute 'mt_enabled',
          :description => 'Enable Transformation',
          :default => 'false',
          :format => {
              :help => 'Disable / Enable notification message transformation',
              :category => '4.Transformation',
              :form => {'field' => 'checkbox'},
              :order => 1
          }

attribute 'mt_id',
          :description => 'Transformer ID',
          :required => 'required',
          :default => 'hpom',
          :format => {
              :help => 'A unique id for message transformer. Eg: HPOM',
              :category => '4.Transformation',
              :filter => {'all' => {'visible' => 'mt_enabled:eq:true'}},
              :order => 2
          }

attribute 'mt_impl_class',
          :description => 'Transformer Class',
          :required => 'required',
          :default => 'com.oneops.antenna.domain.transform.impl.HPOMTransformer',
          :format => {
              :help => 'Message transformer impl class. The class should be of type com.oneops.antenna.domain.transform.Transformer .',
              :category => '4.Transformation',
              :filter => {'all' => {'visible' => 'mt_enabled:eq:true'}},
              :order => 3
          }

# Message dispatching
attribute 'msg_dispatch',
          :description => 'Message Dispatching',
          :required => 'required',
          :default => 'sync',
          :format => {
              :help => 'OneOps event dispatching method. Can be synchronous or asynchronous.',
              :category => '5.Dispatching',
              :order => 1,
              :form => {'field' => 'select', 'options_for_select' => [
                  ['sync', 'sync'],
                  ['async', 'async']]}
          }


