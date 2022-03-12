//存放主要交互逻辑代码
// javascript 模块化
//seckill.eatail.init(params)
var seckill ={

    //封装秒杀相关ajax的 地址url
    URL : {
        now : function () {
            return '/seckill/time/now';
        },
        exposer:function (seckillId) {
            return '/seckill/'+seckillId+'/exposer';
        },
        execution:function (seckillId,md5) {
            return '/seckill/'+seckillId+'/'+md5+'/execution';
        }

    },

    //将验证手机号提取到上层，方便其他地方使用
    validatePhone:function(phone){
        if(phone && phone.length == 11 && !isNaN(phone)){
            return true;
        }else{
            return false;
        }
    },

    handleSeckillkill:function(seckillId,node){
        //获取秒杀地址，控制显示逻辑，执行秒杀
        node.hide()
            .html('<button class="btn btn-primary btn-lg" id="killBtn">开始秒杀</button>');  //按钮
        $.post(seckill.URL.exposer(seckillId),{},function (result) {
            //在回调函数中，执行交互流程
            if(result  && result['success']){
                var exposer = result['data'];
                if(exposer['exposed']){
                    //开启秒杀
                    //获取秒杀地址
                    var md5=exposer['md5'];
                    var killUrl=seckill.URL.execution(seckillId,md5);
                    console.log('killUrl:'+killUrl);
                    //绑定一次点击事件
                    $('#killBtn').one('click',function () {
                        //执行秒杀请求
                        //1：禁用按钮
                        $(this).addClass('disabled');
                        //2：发送秒杀请求post，执行秒杀
                        $.post(killUrl,{},function (result) {
                            if(result && result['success']){
                                var killResult = result['data'];
                                var state=killResult['state'];
                                var stateInfo=killResult['stateInfo'];
                                //3：显示秒杀结果
                                node.html('<span class="label label-success">'+stateInfo+'</span>');
                            }
                        });
                    });
                    node.show();    //重新显示node
                }else{
                    //未开启秒杀
                    var now=exposer['now'];
                    var start=exposer['start'];
                    var end=exposer['end'];
                    //重新计算计时逻辑
                    seckill.countdown(seckillId,now,start,end);
                }
            }
        });
    },

    //抽象出来，减少代码长度
    countdown:function(seckillId,nowTime,startTime,endTime){
        var seckillBox=$('#seckill-box');
        //时间判断
        if(nowTime>endTime){
            //秒杀结束
            seckillBox.html('秒杀结束!');
        }else if(nowTime < startTime){
            //秒杀未开始，countdown插件计时,计时事件绑定
            var  killTime=new Date(startTime + 1000); //防止用户计时偏移
            seckillBox.countdown(killTime,function(event) {
                //时间格式
                var format=event.strftime('秒杀倒计时：%D天 %H时 %M分 %S秒');
                seckillBox.html(format);
            }).on('finish.countdown',function () {
                //时间完成后回调事件
                //获取秒杀地址，控制显示逻辑，执行秒杀
                seckill.handleSeckillkill(seckillId,seckillBox);
            })
        }else{
            //秒杀开始
            seckill.handleSeckillkill(seckillId,seckillBox);
        }
    },

    //详情页秒杀逻辑
    detail : {
        //详情页初始化
        init : function(params){
            //用户手机验证和登陆，计时交互
            //规划交互流程
            //1、验证手机
            //在cookie中查找手机号
            var killPhone = $.cookie('killPhone');
            //验证手机号
            if(!seckill.validatePhone(killPhone)){
                //如果没有登陆，绑定手机号
                //取jsp中的弹出层,空控制输出
                var killPhoneModal=$('#killPhoneModal');
                //显示弹出层
                killPhoneModal.modal({
                    show:true,  //显示弹出层
                    backdrop:'static',  //进制位置关闭
                    keyboard:false  //关闭键盘事件
                })
                $('#killPhoneBtn').click(function () {
                    //点击事件
                    var inputPhone = $('#killPhoneKey').val();
                    if(seckill.validatePhone(inputPhone)){
                        //电话写入cookie
                        $.cookie('killPhone',inputPhone,{expires:7,path:'/seckill'}); //cookie有效期7天，存放到/seckill模块下
                        //验证通过，刷新页面
                        window.location.reload();
                    }else{
                        //先隐藏，再显示出来
                        $('#killPhoneMessage').hide().html('<label class="label  label-danger">手机号错误!</label>').show(300);
                    }
                });
            }
            //运行完上边的逻辑后就会有手机号
            //计时交互
            //ajax
            var seckillId=params['seckillId'];
            var startTime=params['startTime'];
            var endTime=params['endTime'];
            $.get(seckill.URL.now(),{},function (result) {
                if(result && result['success']){
                    var nowTime=result['data'];
                    //进行时间判断
                    seckill.countdown(seckillId,nowTime,startTime,endTime);
                }else{
                    console.log('result:'+result);
                }
            });

        }
    }
}
