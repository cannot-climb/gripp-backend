<?php
require_once './dotenv/Processor/IProcessor.php';
require_once './dotenv/Processor/AbstractProcessor.php';
require_once './dotenv/Processor/BooleanProcessor.php';
require_once './dotenv/Processor/QuotedProcessor.php';
require_once './dotenv/DotEnv.php';

(new DevCoder\DotEnv(__DIR__ . '/.env'))->load();

function adminer_object() {
  class AdminerSoftware extends Adminer {
    function name() {
      return 'gripp';
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

require './editor.php';
