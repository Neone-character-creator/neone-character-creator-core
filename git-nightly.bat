@ECHO OFF
set 
for /f "delims=" %%a in ('git rev-parse --abbrev-ref HEAD') do set origin_branch=%%a
git checkout nightly
git add .
git commit -m "Automatic commit at %Date%"
git push origin --all
git checkout %origin_branch%