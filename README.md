Please do following steps before start editing

//First use git branch to check where are you, be sure you are on the * master
git branch

//Then check which remote git you are attaching
git remote -v

//Please use the one, which address is https://github.com/drsauron2024/HungrySnake.git or git@github.com:drsauron2024/HungrySnake.git
//Tap the branch name before the address
EX.
  PS C:\Users\drsau\Desktop\HungrySnake> git remote -v
  origin  git@github.com:drsauron2024/HungrySnake.git (fetch)
  origin  git@github.com:drsauron2024/HungrySnake.git (push)
  //Then you should tap ‘origin master’ after git pull command

//Update your own code before start working
git pull [The Name before the right address] master

//After you finish the work use git push to update the remote git
git push
