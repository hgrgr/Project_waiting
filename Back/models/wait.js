module.exports = (sequelize, DataTypes)=>(
    sequelize.define('wait',{
        hostname: {
            type: DataTypes.STRING(20),
            allowNull: false,
        },
        userid: {
            type: DataTypes.STRING(20),
            allowNull: false,
        },
        waitnumber: {
            type: DataTypes.INTEGER,
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
    },{
        timestamps: true,
        paranoid: true,
        charset:'utf8',
        collate:'utf8_general_ci',
    })
);