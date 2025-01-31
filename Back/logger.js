const { createLogger, format, transports} = require('winston');

const logger = createLogger({
    level: 'iofo',
    format: format.json(),
    transports: [
        new transports.File({filename: 'combined.log'}),
        new transports.File({filename: 'error.log', level: 'error'}),
    ],
});

if(process.env.BACK !='production'){
    logger.add(new transports.Console({ format: format.simple()}));
}

module.exports = logger;