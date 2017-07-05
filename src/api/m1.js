//test function(){}()
(function(){
	mPrint("(function(){}())")
}())

{
	//code block
	mPrint("{}")
}

var MOB = [
   {id:5898, name: "雞"},
   {id:5899, name: "牛"},
   {id:5900, name: "豬"},
   {id:5901, name: "羊"},
   {id:22286, name: "狼"},
   {id:5904, name: "蘑菇牛"},
   {id:1807, name: "村民"},
   {id:199456, name: "殭屍"},
   {id:2849, name: "苦力帕"},
   {id:68386, name: "骷髏射手"},
   {id:264995, name: "蜘蛛"},
   {id:68388, name: "殭屍猪人"},
   {id:264999, name: "蠹魚"},
   {id:87, name: "安德"},
   {id:2853, name: "史萊姆"}
]
{
	//test for in
	for(index in MOB){
		mPrint("id = "+MOB[index].id+", name = "+MOB[index].name)
	}
}

function show(){
	mPrint("invoke function show() in m1.js")
	mPrint("Const.HELLO = "+Const.HELLO)
	mPrint("Const.TWO = "+Const.TWO)
}