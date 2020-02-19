require('dotenv').config();

module.exports={
  "development": {
    "username": "root",
    "password": process.env.DB_PASS,
    "database": "waiting",
    "host": "127.0.0.1",
    "dialect": "mysql",
    "operatorsAliases": false
  },
  "test": {
    "username": "root",
    "password": process.env.DB_PASS,
    "database": "database_test",
    "host": "127.0.0.1",
    "dialect": "mysql",
    "operatorsAliases": false
  },
  "development": {
    "username": "root",
    "password": process.env.DB_PASS,
    "database": "waiting",
    "host": "127.0.0.1",
    "dialect": "mysql",
    "operatorsAliases": false,
    "logging": false,
  }
};
