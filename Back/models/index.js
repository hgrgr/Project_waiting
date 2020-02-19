const Sequelize = require('sequelize');
const env = process.env.NODE_ENV || 'development';
const config = require('../config/config')[env];
const db = {};

const sequelize = new Sequelize(
  config.database, config.username, config.password, config,
);

db.sequelize = sequelize;
db.Sequelize = Sequelize;

db.User = require('./user')(sequelize,Sequelize);//유저 db
db.Host = require('./host')(sequelize,Sequelize);//호스트 db
db.Wait = require('./wait')(sequelize,Sequelize);//대기자 db

module.exports=db;
