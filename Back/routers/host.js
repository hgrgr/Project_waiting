const express = require('express');
const { User, Host,Wait,Sequelize:{ Op } } = require('../models');
var FCM = require('fcm-node');
var serverKey=process.env.SEVER_KEY;
var fcm = new FCM(serverKey);
const router = express.Router();

router.post('/delete', async (req,res,next)=>{//호스트 삭제 요청 - 대기자,상태 체크해야함
    console.log(req.body);
    const {hostname} = req.body;
    try{
        const teHost = await Host.findOne({
            attributes: ['waitnumber','currentnumber','statuse'],
            where:{hostname}
        })
        if(!teHost){//없으면 에러
            res.write("err");
            return res.end();
        }
        if(teHost.statuse === "stop" &&(teHost.currentnumber - teHost.waitnumber) ===0){//정지 + 대기자 0 삭제가능
            if(teHost.waitnumber>0){//대기자 한명이라도 있을시
                const teWait = await Wait.destroy({where:{hostname,waitnumber: teHost.waitnumber}});
                if(!teWait){//삭제오류시
                    res.write("err");
                    return res.end();
                }
            }
            const test = await Host.destroy({//그 호스트 날려버림
                where:{hostname},
            })
            if(!test){//에러 발생시
                res.write("err");
                return res.end();
            }
            res.write("success");
            return res.end();
        }
        else{//삭제 불가능
            res.write("fail");
            return res.end();
        }
    }catch(err){
        res.write("err");
        res.end();
        console.error(err);
        return next(err);//굳이 필요한가?
    }
});

router.post('/reset', async (req,res,next)=>{//웨이팅 정보 초기화 한다
    console.log(req.body);
    const {hostname}=req.body;
    try{
        const teHost = await Host.findOne({
            attributes: ['waitnumber','currentnumber','statuse'],
            where: {hostname}
        });
        if(!teHost){//없을 경우 에러임
            res.write("err");
            return res.end();
        }
        console.log(teHost.waitnumber,teHost.currentnumber);
        if(teHost.statuse == "stop" && (teHost.waitnumber - teHost.currentnumber) ===0 ){//stop + 대기자 없으면
            
            if(teHost.currentnumber>0){///0보다 클경우 마지막 웨이팅 정보 삭제해줘야함
                const teWait = await Wait.destroy({//호스트이름과 웨이팅 정보로 삭제
                    where:{hostname,waitnumber: teHost.currentnumber}
                })
                if(!teWait){//없을 경우 에러임
                    res.write("err");
                    return res.end();
                }
            }
            const test = await Host.update({waitnumber: 0, currentnumber: 0},{where:{hostname}});
            if(!test){//업데이트 실패시 에러
                res.write("err");
                return res.end();
            }
            res.write("ok");
            return res.end();
        }
        res.write("fail");
        return res.end();

    }catch(err){
        res.write(err);
        res.end();
        console.error(err);
        return next(err);//굳이 필요한가?
    }
});

router.post('/alarm', async (req,res,next)=>{//알람 받을수 변경해줌
    console.log(req.body);
    const { hostname, alarm } = req.body;
    try{
        const teHost = await Host.update({
            alarmnumber: alarm
        },{
            where: {hostname}
        })
        if(!teHost){//업데이트 실패시
            res.write("server error fail - please retry");
            return res.end();
        }
        res.write(alarm);
        return res.end();
    }catch(err){
        res.write(err);
        res.end();
        console.error(err);
        return next(err);//굳이 필요한가?
    }
})
router.post('/pass',async (req,res,next)=>{//대기자수 +1 해줌 - 알람 보내줘야함
    req.connection.setTimeout(60*3*1000);//3분동안 타임 아웃 발생 x
    console.log(req.body);
    const {hostname} = req.body;
    try{
        const teHost = await Host.findOne({//현재상태꺼내옴 (현재 처리하는 번호,알림 보낼수 )
            attributes: ['currentnumber','alarmnumber','waitnumber'],
            where:{hostname}
        });
        if(teHost.waitnumber -teHost.currentnumber <=0){//웨이팅 하는 사람 없을경우
            console.log("hi");
            res.write("NO waiter");
            return res.end();
        }
        else{//웨이팅이 1명이라도 있을경우
            if(teHost.currentnumber > 0 ){//0보다 크면 삭제해줌
                console.log("1");
                const teWait = await Wait.destroy({//Wait디비에 처리해준 클리이언트 정보 삭제하는것
                where:{hostname,waitnumber: teHost.currentnumber}});
                console.log("2");
                if(!teWait){//삭제 실패시
                    res.write(err);
                    return res.end();
                }
           }

             const alnum = teHost.currentnumber+1;//알람 처음받을 클라이언트
             console.log("2.5");
            const teUser = await Wait.findAll({//알랍 받을 애들 다 찾아줌
                attributes: ['userid'],
                where:{hostname, waitnumber: {[Op.lte]: alnum+teHost.alarmnumber}}
            });
            console.log("3");
            for(let i=0;i<teUser.length;i++){//알람 보낼인수 뽑아옴 (이렇게 해도 되는이유 findAll에서 이미 알람수도 반영되어 있음)
               
                //해당 유저들 db조회해서 토큰 정보 뽑은뒤 파이어베이스에 FCM 요청해야함
                const pushUser = await User.findOne({//유저의 토큰 뽑아온다-상태도 뽑아서 로그인 한애들에게 보낸다
                    attributes: ['token','status'],
                    where:{userid: teUser[i].userid}
                })
                let userToken = pushUser.token;
                if(userToken !=="null"){//토큰 값 있을경우만 푸시 알람 보내준다
                    console.log("토큰있네요",userToken);
                    const message = {
                        to:userToken,
                        // notification:{
                        //     title:`호스트: ${hostname}`,
                        //     body: `대기자 ${i}명 남았습니다`,
                        //     sound: "default",
                        //     click_action: "FCM_PLUGIN_ACTIVITY",
                        //     icon: "fcm_push_icon"  
                        // },
                         // 메시지 중요도
                         priority: "high",
                         data: {
                            title:`호스트: ${hostname}`,
                            body: `대기자 ${i}명 남았습니다`,
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
             
            }//login중인 사람에게 알람 보내줌
            console.log("4");
            const testHost = await Host.update({currentnumber: alnum},
                {where: {hostname}});

            if(!testHost){//업데이트 실패했을경우
                res.write("err");
                return res.end();
            }
            console.log("5");
            res.write(String(alnum));
            console.log("6");
            return res.end();//변경된 현재 처리 번호 전송해준다
        }

    }catch(err){
        res.write(err);
        return res.end();
        console.error(err);
        return next(err);//굳이 필요한가?
    }
});

router.post('/state', async(req,res,next)=>{//호스트 상태 갱신해줌
    console.log(req.body);
    const { hostname, state } = req.body;
    try{
        const teHost = await Host.update({
            statuse: state
        },{
            where: {hostname}
        });
        console.log("this ",teHost);
        if(!teHost){//업데이트 못했을경우
            res.write("server error fail - please retry");
            return res.end();
        }
        res.write(state);
        return res.end();

    }catch(err){
        res.write(err);
        res.end();
        console.error(err);
        return next(err);//굳이 필요한가?
    }
});

router.post('/gethost', async (req,res,next)=>{//사용자가 선택한 호스트 정보 가져옴
    console.log("실행");
    console.log(req.body);
    const {hostname} = req.body;
    try{
      const teHost = await Host.findOne({
           attributes: ['statuse', 'waitnumber','currentnumber', 'alarmnumber'],
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

router.post('/gethosts',async (req,res,next)=>{//사용자가 등록한 호스트 정보들 가져옴
    console.log(req.body);
    const { userid } =req.body;
    if(!userid){
        res.write("login error Please return to Logout Page");
        return res.end();
    }
    try{
        const teHost = await Host.findAll({//3가지 칼럼만 보내준다
            attributes: ['hostname','latitude','lotitude'],
        
        
            where: { userid}
        });
        if(!teHost){//등록 한게 하나도 없을경우
            res.write("pleas post your host")
            return res.end();
        }
        const sendHostData = {  
            hostData: teHost,
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

router.post('/join',async (req,res,next)=>{//호스트 등록해줌
    console.log(req.body);
    const {addone,name,addtwo,userid} = req.body;//비구조화 할당
    if(!addone || !name || !addtwo || !userid ){//한곳이라도 비면 거부
        res.write("please input data");
        return res.end();
    }
    try{//데이터 다있을경우 디비에 넣어줌
        const teUser = await User.findOne({where: {userid}});//사용자 정보 있을경우
        const teHost = await Host.findOne({
            where: { hostname: name}
        });
        if(!teUser){//등록된 사용자 아닐시
            res.write("login error Please return to Logout Page");
            return res.end();
        }
        if(teHost){//이미 같은 호스트명 있을시
            res.write("host name is Already");
            return res.end();
        }
 
        //겹치는것 없고 디비에 저장 가능할때
        await Host.create({
            hostname: name,
            userid,
            latitude: addone,
            lotitude: addtwo,
           statuse: "stop",
           waitnumber: 0,
           currentnumber: 0,
           alarmnumber: 5,
        });
        console.log("host join");
        res.write('join complete');
        return res.end();

    }catch(err){
        res.write('errer');
        res.end();
        console.error(error);
        return next(error);//굳이 필요한가?
    }
    
});

module.exports = router;