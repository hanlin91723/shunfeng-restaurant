var __clientTokenKey = 'clientToken';
var user = 'user';//用户存储名
var cardinality = 1000000;//ONB数量基础倍数
var successCode = '000000000';//区块链成功编码
var defaultShow = '****';//钱包余额默认显示
//ONB钱包区块链请求地址
var chainApi = [
	"https://114.116.88.62/",
	"https://114.116.68.154/"
];

var systemApi='https://jiudiansc.cc:8443/TransServlet';
//var systemApi='http://192.168.0.43:8081/TransServlet';
var imageSever= systemApi +'?ext=jpg&transCode=B00022&clientToken=';//图片服务


//1.5秒的提示消息
function creatTips(tips){
	var createDiv=document.createElement("div");
	createDiv.style.position="fixed";
	createDiv.style.top='0';
	createDiv.style.bottom='0';
	createDiv.style.left="0"; 
	createDiv.style.right="0"; 
	createDiv.style.zIndex="9999999";
	createDiv.style.background="rgba(0,0,0,0)";
	createDiv.style.textAlign="center"; 
	document.body.appendChild(createDiv);
	var createP=document.createElement("p");
	createP.style.position="absolute"; 
	createP.style.top="50%";
	createP.style.left="50%";  
	createP.style.transform="translate(-50%,-50%)";
	createP.style.fontSize="0.46rem";
	createP.style.color="#fff";
	createP.style.margin="auto";  
	createP.style.padding="0.3rem 0.5rem";  
	createP.style.borderRadius="0.2rem"; 
	createP.style.background="rgba(0,0,0,0.8)";
	createP.innerHTML=tips; 
	createDiv.appendChild(createP);
	var time=setTimeout(function(){
		document.body.removeChild(createDiv);
		clearTimeout(time);
	},1500);	
}

