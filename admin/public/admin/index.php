<?php
define('ROOT_DIR', dirname(dirname(__DIR__)));
require_once ROOT_DIR . '/vendor/dotenv/Processor/IProcessor.php';
require_once ROOT_DIR . '/vendor/dotenv/Processor/AbstractProcessor.php';
require_once ROOT_DIR . '/vendor/dotenv/Processor/BooleanProcessor.php';
require_once ROOT_DIR . '/vendor/dotenv/Processor/QuotedProcessor.php';
require_once ROOT_DIR . '/vendor/dotenv/DotEnv.php';

(new DevCoder\DotEnv(ROOT_DIR . '/.env'))->load();

function adminer_object() {
  class AdminerSoftware extends Adminer {
    function name() {
      return 'gripp';
    }

    function permanentLogin($create = false) {
      return md5('sa:' . getenv('GRIPP_ADMIN_NAME') . ':lt:' . getenv('GRIPP_ADMIN_PASSWORD_RAW') . ':ra:' . getenv('GRIPP_DB_HOSTNAME') . ':nd');
    }

    function credentials() {
      return array(getenv('GRIPP_DB_HOSTNAME') . ':' . getenv('GRIPP_DB_PORT'), getenv('GRIPP_DB_USERNAME'), getenv('GRIPP_DB_PASSWORD'));
    }

    function database() {
      return getenv('GRIPP_DB_SCHEMA');
    }

    function login($login, $password) {
      return ($login == getenv('GRIPP_ADMIN_NAME') && $password == getenv('GRIPP_ADMIN_PASSWORD_RAW'));
    }
  }

  return new AdminerSoftware;
}

require ROOT_DIR . '/vendor/adminer/editor.php';
