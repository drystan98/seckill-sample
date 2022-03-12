<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<!DOCTYPE html>
<html>
<head>
    <title>秒杀详情页</title>
    <!--包含-->
    <%@include file="common/head.jsp"%>
</head>
<body>
    <div class="container">
        <div class="panel panel-default text-center">
            <div class="panel-heading">
                <h1>${seckill.name}</h1>
            </div>
            <div class="panel-body">
                <h2 class="text-danger">
                    <!--显示time图标-->
                    <span class="glyphicon glyphicon-time"></span>
                    <!--展示倒计时-->
                    <span class="glyphicon" id="seckill-box"></span>
                </h2>
            </div>
        </div>
    </div>
    <!--登陆弹出层，输入电话-->
    <!--modal插件-->
    <!--利用id埋点-->
    <div id="killPhoneModal" class="modal fade">
        <div class="modal-dialog">
            <div class="modal-content">
                <div class="modal-header">
                    <h3 class="modal-title" text-center>
                        <span class="glyphicon glyphicon-phone"></span>
                    </h3>
                </div>
                <div class="modal-body">
                    <div class="row">
                        <div class="col-xs-8 col-xs-offset-2">
                            <input type="text" name="killPhone" id="killPhoneKey"
                                    placeholder="填写手机号" class="form-control">
                        </div>
                    </div>
                </div>
                <div class="modal-footer">
                    <!--验证信息-->
                    <span  id="killPhoneMessage" class="glyphicon"></span>
                    <button type="button" id="killPhoneBtn" class="btn btn-success">
                        <span class="glyphicon glyphicon-phone"></span>
                        提交
                    </button>
                </div>
            </div>
        </div>
    </div>
</body>
<!-- 新 Bootstrap 核心 CSS 文件 -->
<link href="https://cdn.staticfile.org/twitter-bootstrap/3.3.7/css/bootstrap.min.css" rel="stylesheet">
<!-- jQuery文件。务必在bootstrap.min.js 之前引入 -->
<script src="https://cdn.staticfile.org/jquery/2.1.1/jquery.min.js"></script>
<!-- 最新的 Bootstrap 核心 JavaScript 文件 -->
<script src="https://cdn.staticfile.org/twitter-bootstrap/3.3.7/js/bootstrap.min.js"></script>

<!--使用CDN获取公共js http://www.bootcdn.cn/-->
<!--JQuery cookie操作插件-->
<script src="http://cdn.bootcss.com/jquery-cookie/1.4.1/jquery.cookie.min.js"></script>
<!--JQuery  countDown倒计时插件-->
<script src="https://cdn.staticfile.org/jquery.countdown/2.1.0/jquery.countdown.min.js"></script>

<!--开始编写交互逻辑-->
<script src=/resources/script/seckill.js type="text/javascript"></script>
<script type="text/javascript">
    $(function () {
        //使用EL表达式传入参数
       seckill.detail.init({
            seckillId:${seckill.seckillId}, //id
           startTime:${seckill.startTime.time}, //毫秒
           endTime:${seckill.endTime.time} //毫秒
       });
    });
</script>
</html>
