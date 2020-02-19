module.exports = (sequelize, DataTypes)=>(
    sequelize.define('host',{
        hostname: {
            type: DataTypes.STRING(20),
            allowNull: false,
            
        },
        userid: {
            type: DataTypes.STRING(40),
            allowNull: false,
        },
        latitude: {
            type: DataTypes.STRING(30),
            allowNull: false,
        },
        lotitude: {
            type: DataTypes.STRING(30),
            allowNull: false,
        },
        statuse: {
            type: DataTypes.STRING(10),
            allowNull: true,
        },
        waitnumber: {
            type: DataTypes.INTEGER,
            allowNull: true,
        },
        currentnumber: {
            type: DataTypes.INTEGER,
            allowNull: true,
        },
        alarmnumber: {
            type: DataTypes.INTEGER,
            allowNull: true,
        }
    },{
        timestamps: true,
        paranoid: true,
        charset:'utf8',
        collate:'utf8_general_ci',
    })
);