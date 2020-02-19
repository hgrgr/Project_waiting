const express = require('express');
const cookieParser = require('cookie-parser');
const morgan = require('morgan');
const path = require('path');
const session = require('express-session');
const flash = require('connect-flash');
const passport = require('passport');
const helmet = require('helmet');
const hpp = require('hpp');

require('dotenv').config();


const indexRouter = require('./routers/');
const authRouter = require('./routers/auth');
const hostRouter = require('./routers/host');
const clientRouter =require('./routers/client');
const logger = require('./logger');

const { sequelize } = require('./models');
//const passportConfig = require('./passport');

const app = express();
sequelize.sync();
//passportConfig(passport);

app.set('view engine','pug');
app.set('views', path.join(__dirname,'views'));
app.set('port',process.env.PORT || 8002); 

if(process.env.BACK ==='production'){
    app.use(morgan('combined'));
    app.use(helmet());
    app.use(hpp());
}else{
    app.use(morgan('dev'));
}

app.use(express.static(path.join(__dirname,'public')));
app.use(express.json());
app.use(express.urlencoded({ extended: false}));
app.use(cookieParser(process.env.COOKIE_SECRET));
app.use(session({
    resave: false,
    saveUninitialized: false,
    secret: process.env.COOKIE_SECRET,
    cookie: {
        httpOnly: true,
        secure: false,
    },
}));
app.use(flash());
//app.use(passport.initialize());
//app.use(passport.session());


app.use('/', indexRouter);
app.use('/auth',authRouter);
app.use('/host',hostRouter);
app.use('/client',clientRouter);

app.use((req,res,next)=>{
    const err = new Error('Not Found');
    err.status = 404;
    next(err);
});

app.use((err,req,res)=>{
    res.locals.message = err.message;
    res.locals.err = req.app.get('env') ==='development'? err : {};
    res.status(err.status || 500);
    res.write('error');
    return res.end();
});

app.listen(app.get('port'),()=>{
    console.log(`${app.get('port')} port listen`);
});