const express = require('express');

const router = express.Router();

router.get('/',(req,res,next)=>{
    console.log(req);
    return res.json(req);
})

module.exports = router;