# Be sure to restart your server when you modify this file.

# Your secret key for verifying the integrity of signed cookies.
# If you change this key, all old signed cookies will become invalid!
# Make sure the secret is at least 30 characters and all random,
# no regular words or you'll be exposed to dictionary attacks.

# TODO 'secret_token' (and this whole initializer) can be removed after a while, it still needed only during
# transition period so Rails can convert existing signed cookie-based sessions to new encrypted cookies.
Display::Application.config.secret_token = 'f382c625da4d9fa0c013f16f05426901dae18edb014c23fcd1dae7d88003109da7e527f39f8c94460bd59e6a18a739b4ce16f87410240e95c47175467882cccd'
