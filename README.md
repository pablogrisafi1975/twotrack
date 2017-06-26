# twotrack
A java implementation of railway oriented programming

TwoTrack is a way to handling errors in a functional way. The idea is that a typical java method

service.doSomething(mainObject, param1, parm2) throws IOException

gets Wrapped as a TwoTrack that return either an ok or an error, and the next method is only called on ok

so if you list of methods was


transferMoney(userId1, userId2, amount){
   User user1 = userDAO.find(userId1);
   if(user1 == null){
   	return "User with id1 not found";
   }
   User user2 = userDAO.find(userId2);
   if(user2 == null){
   	return "User with id2 not found";
   }
   
   boolean result = true;
   try{
   	result = moneyTransfer(user1, user2, amount);
   }catch(Exception ex){
   	return "unexpected error transfering money"
   }
   if(result == true){
   	return "money was transfered from userId1 to userId2";
   }else{
   	return "money was NOT transfered from userId1 to userId2 because reasons";
   }
   
   
}
 



Result: a generic Result container, can be either Ok or Fail, both contain generic data

ResultStr: a less generic Result container, that can contain only one error as a String. Ok data is still generic

FailMsg: A specific Fail class that can contain only one error as a key + params + exception. It is expected to be used with
MessageFormat. Ok data is still generic

ResultMsg: a less generic Result container, that can contain only one error as a FailMsg. Ok data is still generic.


