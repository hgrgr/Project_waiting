//로그인 관련 - 회원가입 로그인 로그아웃
const express = require('express');
const passport = require('passport');
const bcrypt = require('bcrypt');
const { User } = require('../models');

const router = express.Router();


router.post('/join', async (req,res,next)=>{
    console.log(req.body);
    const{ userid, password, name } = req.body;//앱에서 보내줌
    if(!userid || !password || !name ){//한곳이라도 빈곳 있으면 오류 돌려보냄
        res.write("No");
        return res.end();
    }
    try{
        const teUser = await User.findOne({//중복된 아이디 있는지 검색
            where: {userid}
        });
        if(teUser){//이미 아이디 존재할 경우 앱에 알려줌
            res.write("already");
            return res.end();
        }
        
        const hash = await bcrypt.hash(password,12);//비밀번호 암호화 해서 저장해야함
       
        await User.create({
            userid,
            name,
            password: hash,
    
        });
        console.log('회원가입 완료');
        res.write("OK");
        return res.end();
    }catch(error){
        res.write('errer');
        res.end();
        console.error(error);
        
        return next(error);//굳이 필요한가?
    }
});


router.post('/login',async (req,res,next)=>{//로그인 기능 - 디비 조회 해야함 /통신은 JSON사용
    console.log(req.body);
  const{ userid, password, token } = req.body;
    if(!userid || !password || !token )//정보 안들어 온경우
    {
        res.write("input id or password");
        return res.end();
    }
    try{
       // console.log("id and password",userid,password);
       
        const users = await User.findOne({where: {userid}});
        if(users.status == 'login'){//이미 로그인 되어있는경우
            const login_res = {
                status: 'already',
                name: 'null',
            };
            res.write(JSON.stringify(login_res));
            return res.end();
        }
        console.log('실행2');
        if(users){//db에 유저 정보 있는경우
        
            const result = await bcrypt.compare(password, users.password);//비밀번호 체크
            
            if(result){//로그인 성공시
                await User.update({status: 'login',   token: token, },{where: { userid}});
                const login_res = {
                    status: 'success',
                    name: users.name,
                  
                };
                res.write(JSON.stringify(login_res));
                return res.end();
            }
            else{
                const login_res = {
                    status: 'fail',
                    name: 'null',
                };
                res.write(JSON.stringify(login_res));
              //  res.write('id or password is wrong');
                return res.end();
            } 
        }
        else{
            const login_res = {
                status: 'fail',
                name: 'null',
            };
            res.write(JSON.stringify(login_res));
         //   res.write('id or password is wrong');
            return res.end();
        }
    }catch(err){
        console.error(err);
        res.write('error');
        return res.end();
    }

});


router.post('/logout', async (req,res,next)=>{//로그아웃시 디비에서 user 상태 logout으로 수정
    console.log(req.body);
    const { userid } = req.body;
    const users = await User.update({status: 'logout', token: 'null',},{where: {userid}})//userid 찾아서 상태 logout으로 바꿔짐
    if(users){//로그아웃 성공시

        const logout_res = {
                    status: 'success',
                    name: 'null',
                };
                res.write(JSON.stringify(logout_res));
                return res.end();
    }
    else{//로그아웃 실패시
        const logout_res = {
            status: 'fail',
            name: 'null',
        };
        res.write(JSON.stringify(logout_res));
        return res.end();
}
    }
);
module.exports = router;


