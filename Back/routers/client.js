const express = require('express');
const {User,Host, Wait,Sequelize:{Op}} = require('../models');
var FCM = require('fcm-node');
var serverKey=process.env.SEVER_KEY;
var fcm = new FCM(serverKey);
const router = express.Router();

router.post('/delete',async (req,res,next)=>{
    console.log(req.body);
    const {hostname,userid}=req.body;
    try{
        //현재 내번호 부터 가져와야함
        console.log("1");
        const teWait = await Wait.findOne({//해당하는 웨이팅넘버 가져온다
            attributes: ['waitnumber'],
            where: {hostname,userid}
        });
        const current = await Host.findOne({//호스트의 현재 번호 가져온다
            attributes:['currentnumber'],
            where:{hostname},
        })
        if(teWait.waitnumber === current.currentnumber){//현재 자신의 차례일경우 취소 못함 - 이걸로 나중에 제한 가능
            console.log("dfsdf");
            res.write("myturn");
            return res.end();
        }
        const test = await Wait.destroy({where:{hostname,userid}})//삭제 해줘야한다
        if(!test){//못찾으면 에러
            res.write("err");
            return res.end();
        }
        const mynumber = teWait.waitnumber;//현재 자신의 웨이팅 번호 저장해줌 
        console.log(2);
        const changeNum = await Wait.findAll(//대기번호 당겨야할 애들 id와 현재 자신 번호 가져옴
            {
                attributes: ['id','waitnumber','userid'],
                where:{
                    waitnumber: { [Op.gt]:mynumber}
                }
            });
        if(!changeNum){//업데이트할 애들 없으면
            console.log(2.5);
            res.write("NO");
            return res.end();
        } 
        console.log(changeNum.length);
        for(let i=0;i<changeNum.length;i++){//대기번호 당겨준다 -
            await Wait.update({waitnumber: changeNum[i].waitnumber-1},{where: {id: changeNum[i].id}});
        }
        for(let i=0;i<changeNum.length;i++){//당겨진 친구들 알림 보내준다
            const pushUser = await User.findOne({
                attributes: ['token','status'],
                where: {userid: changeNum[i].userid}
            });
            let userToken = pushUser.token;
            if(userToken !=="null"){
                const message = {
                    to:userToken,
                    priority: "high",
                    data : {
                        title:`호스트: ${hostname}`,
                        body: `대기번호가 당겨졌습니다`,
                    }
                }
                fcm.send(message,(err, res)=>{
                    if(err){
                        res.write(err);
                        return res.end();
                      
                    }else{
                        console.log("Fcm요청 전송함");
                    }
                });
            }
            
        }//fcm보내기
        const changeHost = await Host.findOne({
            attributes:['waitnumber'],
            where:{hostname},
        });
        await Host.update({waitnumber: changeHost.waitnumber-1}, {where: {hostname}});//호스트의 웨이팅 번호 감소시켜줌
        console.log(3);
        res.write("OK");
        return res.end();
    }catch(err){

    }
})
router.post('/mywaits',async (req,res,next)=>{//userid가 웨이팅한곳 전부 보내줌
    console.log(req.body);
    try{
        const {userid}=req.body;
        const teWait = await Wait.findAll({
            where:{userid}
        })
        if(!teWait){//웨이팅 등록한곳 없을시
            res.write("NO");
            return res.end();
        }
        const sendHostData = {
            hostData: teWait,
        }
        res.write(JSON.stringify(sendHostData));
        return res.end();
    }catch(err){
        res.write(err);
        res.end();
        console.error(err);
        return next(err);//굳이 필요한가?
    }
});

router.post('/mywait',async (req,res,next)=>{//대기순번 가져옴
    console.log(req.body);
    const { hostname,userid } = req.body;
    try{
        const teWait = await Wait.findOne({//나의 웨이트 넘버 보내줌 +
            attributes: ['waitnumber'],
            where:{hostname,userid}
        });
        const teHost = await Host.findOne({//여기서 현재 처리중인 번호 찾아옴
            attributes: ['currentnumber'],
            where:{hostname}
        });
        if(teWait){//존재할경우
            const data = {
                waitnumber: String(teWait.waitnumber),
                currentnumber: String(teHost.currentnumber)
            }     
            res.write(JSON.stringify(data));
            return res.end();
        }
        //없을경우
        res.write("NO");
        return res.end();
    }catch(err){
        res.write(err);
        res.end();
        console.error(err);
        return next(err);//굳이 필요한가?
    }

});
router.post('/waiting', async (req,res,next)=>{//웨이팅 추가해준다
    console.log(req.body);
    const { hostname,userid,latitude,lotitude } = req.body;
    try{
        const teHost = await Host.findOne({
            attributes: ['statuse','waitnumber'],
            where: {hostname},
        })
        if(!teHost){//오류 났을경우
            res.write("NO");
            return res.end();
        }
        let {statuse,waitnumber} = teHost;
        console.log(statuse);
        if(statuse == "start"){//host상태 체크
            const test = await Wait.findOne({//이미 웨이팅 신청했는지 체크
                where:{hostname,userid},
            })
            if(test){//이미 웨이팅 신청한경우
                res.write("already");
                return res.end();
            }
            console.log("실행");
            let putnumber = parseInt(waitnumber) +1;//string ->int 해서 +1 해줌
            putnumber = String(putnumber);//int ->string으로 저장
            const check = await Host.update({
                waitnumber: putnumber
            },{
                where: {hostname},
            })
            if(!check){//update 오류시
                res.write("NO");
                return res.end();
            }
            //성공시 wait 디비에 정보 만들어준다
            const teWait = await Wait.create({
                hostname,
                userid,
                latitude,
                lotitude,
                waitnumber: putnumber,
            })
            if(!teWait){//여기서 실패하면 곤란한데 데이터 일관성 깨짐
                res.write("NO");
                return res.end();
            }
            res.write(putnumber);
            return res.end();

        }else{//웨이팅 막혀 있을경우
            res.write("stop");
            return res.end();
        }
    }catch(err){
        res.write(err);
        res.end();
        console.error(err);
        return next(err);//굳이 필요한가?
    }
});

router.post('/getdata', async (req,res,next)=>{
    console.log("실행");
    console.log(req.body);
    const {hostname} = req.body;
    try{
      const teHost = await Host.findOne({
           attributes: ['statuse', 'waitnumber','currentnumber'],
           where: {hostname}
        })
        if(!teHost){//오류때문에 없을경우 - 
            res.write("NO data");
            return res.end();
        }
        res.write(JSON.stringify(teHost));
        return res.end();

    }catch(err){
        res.write(err);
        res.end();
        console.error(err);
        return next(err);//굳이 필요한가?
    }
});

router.post('/search',async (req,res,next)=>{
    console.log(req.body);
    const {hostname} = req.body;
    try{
        const teHost = await Host.findAll({
            attributes: ['hostname','latitude','lotitude'],
            where: {hostname}
         });
        if(!teHost){//검색결과 없을경우
            res.write("no");
            return res.end();
        }
        const sendHostData = {  //데이터 gson으로 처리할수있게 보내줌
            hostData: teHost,
        }
        res.write(JSON.stringify(sendHostData));
        return res.end();
    }catch(err){
        res.write('errer');
        res.end();
        console.error(error);
        
        return next(error);//굳이 필요한가?
    }
});


module.exports=router;