git add .
@echo off
set /p param=«Î ‰»Îcommit£∫
echo on
git commit -m "%param%"
git pull origin master
git push origin master
pause