module.exports = (sequelize, DataTypes)=>(
    sequelize.define('user',{
        userid: {
            type: DataTypes.STRING(40),
            allowNull: false,
            unique: true,
        },
        password: {
            type: DataTypes.STRING(100),
            allowNull: false,

        },
        name: {
            type: DataTypes.STRING(40),
            allowNull: false,
        },
        status: {
            type: DataTypes.STRING(10),
            allowNull: true,
        },
        token: {
            type: DataTypes.STRING(200),
            allowNull: true,
        }
    },{
        timestamps: true,
        paranoid: true,
        charset:'utf8',
        collate:'utf8_general_ci',
    })
);