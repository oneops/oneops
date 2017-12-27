# encoding: UTF-8
# This file is auto-generated from the current state of the database. Instead
# of editing this file, please use the migrations feature of Active Record to
# incrementally modify your database, and then regenerate this schema definition.
#
# Note that this schema.rb definition is the authoritative source for your
# database schema. If you need to create the application database on another
# system, you should be using db:schema:load, not running all the migrations
# from scratch. The latter is a flawed and unsustainable approach (the more migrations
# you'll amass, the slower it'll run and the greater likelihood for issues).
#
# It's strongly recommended that you check this file into your version control system.

ActiveRecord::Schema.define(version: 20161202025713) do

  # These are extensions that must be enabled in order to support this database
  enable_extension "plpgsql"

  create_table "authentications", force: true do |t|
    t.integer  "user_id"
    t.string   "provider"
    t.string   "uid"
    t.datetime "created_at", null: false
    t.datetime "updated_at", null: false
  end

  create_table "ci_proxies", force: true do |t|
    t.integer  "organization_id",                          null: false
    t.integer  "ci_id",                                    null: false
    t.string   "ci_name",         limit: 100,              null: false
    t.string   "ns_path",         limit: 250,              null: false
    t.datetime "created_at",                               null: false
    t.datetime "updated_at",                               null: false
    t.string   "ci_class_name",   limit: 100, default: "", null: false
  end

  add_index "ci_proxies", ["ci_id"], name: "index_ci_proxies_on_ci_id", unique: true, using: :btree
  add_index "ci_proxies", ["ci_name"], name: "index_ci_proxies_on_ci_name", using: :btree
  add_index "ci_proxies", ["organization_id"], name: "index_ci_proxies_on_organization_id", using: :btree

  create_table "ci_proxies_teams", force: true do |t|
    t.integer "ci_proxy_id"
    t.integer "team_id"
  end

  create_table "group_members", force: true do |t|
    t.integer  "group_id"
    t.integer  "user_id"
    t.boolean  "admin",                 default: false, null: false
    t.string   "created_by", limit: 40,                 null: false
    t.datetime "created_at",                            null: false
    t.datetime "updated_at",                            null: false
  end

  create_table "groups", force: true do |t|
    t.string   "name",        limit: 50
    t.string   "created_by",  limit: 40, null: false
    t.text     "description"
    t.datetime "created_at",             null: false
    t.datetime "updated_at",             null: false
  end

  add_index "groups", ["name"], name: "index_groups_on_name", using: :btree

  create_table "groups_teams", force: true do |t|
    t.integer "group_id"
    t.integer "team_id"
  end

  create_table "invitations", force: true do |t|
    t.string   "email"
    t.string   "token",      limit: 8
    t.string   "comment",    limit: 200
    t.datetime "created_at",             null: false
    t.datetime "updated_at",             null: false
  end

  create_table "misc_docs", force: true do |t|
    t.string   "name",       limit: 50
    t.text     "document"
    t.datetime "created_at",            null: false
    t.datetime "updated_at",            null: false
  end

  add_index "misc_docs", ["name"], name: "index_misc_docs_on_name", unique: true, using: :btree

  create_table "organizations", force: true do |t|
    t.string   "name",         limit: 50
    t.datetime "created_at",                               null: false
    t.datetime "updated_at",                               null: false
    t.integer  "cms_id"
    t.boolean  "assemblies",               default: true,  null: false
    t.boolean  "catalogs",                 default: true,  null: false
    t.boolean  "services",                 default: false, null: false
    t.text     "announcement"
    t.string   "full_name",    limit: 100
  end

  add_index "organizations", ["name"], name: "index_organizations_on_name", unique: true, using: :btree

  create_table "teams", force: true do |t|
    t.string   "name",             limit: 50
    t.integer  "organization_id",                             null: false
    t.datetime "created_at",                                  null: false
    t.datetime "updated_at",                                  null: false
    t.text     "description"
    t.boolean  "design",                      default: false, null: false
    t.boolean  "transition",                  default: false, null: false
    t.boolean  "operations",                  default: false, null: false
    t.boolean  "org_scope",                   default: false, null: false
    t.boolean  "cloud_services",              default: false, null: false
    t.boolean  "cloud_compliance",            default: false, null: false
    t.boolean  "cloud_support",               default: false, null: false
    t.boolean  "manages_access",              default: false, null: false
  end

  add_index "teams", ["organization_id", "name"], name: "index_teams_on_organization_id_and_name", unique: true, using: :btree
  add_index "teams", ["organization_id"], name: "index_teams_on_organization_id", using: :btree

  create_table "teams_users", force: true do |t|
    t.integer  "team_id"
    t.integer  "user_id"
    t.datetime "last_sign_in_at"
  end

  create_table "user_favorites", force: true do |t|
    t.integer "user_id"
    t.integer "ci_proxy_id"
  end

  create_table "user_watches", force: true do |t|
    t.integer "user_id"
    t.integer "ci_proxy_id"
  end

  create_table "users", force: true do |t|
    t.string   "email",                              default: "",   null: false
    t.string   "encrypted_password",     limit: 128, default: "",   null: false
    t.string   "reset_password_token"
    t.datetime "reset_password_sent_at"
    t.datetime "remember_created_at"
    t.integer  "sign_in_count",                      default: 0
    t.datetime "current_sign_in_at"
    t.datetime "last_sign_in_at"
    t.string   "current_sign_in_ip"
    t.string   "last_sign_in_ip"
    t.string   "confirmation_token"
    t.datetime "confirmed_at"
    t.datetime "confirmation_sent_at"
    t.integer  "failed_attempts",                    default: 0
    t.string   "unlock_token"
    t.datetime "locked_at"
    t.integer  "organization_id"
    t.datetime "created_at",                                        null: false
    t.datetime "updated_at",                                        null: false
    t.boolean  "show_wizard",                        default: true, null: false
    t.string   "password_salt"
    t.string   "unconfirmed_email"
    t.datetime "eula_accepted_at"
    t.string   "authentication_token"
    t.string   "name",                   limit: 50
    t.string   "username",               limit: 40
    t.string   "session_token",          limit: 30
  end

  add_index "users", ["authentication_token"], name: "index_users_on_authentication_token", unique: true, using: :btree
  add_index "users", ["confirmation_token"], name: "index_users_on_confirmation_token", unique: true, using: :btree
  add_index "users", ["email"], name: "index_users_on_email", unique: true, using: :btree
  add_index "users", ["reset_password_token"], name: "index_users_on_reset_password_token", unique: true, using: :btree
  add_index "users", ["username"], name: "index_users_on_username", unique: true, using: :btree

end
