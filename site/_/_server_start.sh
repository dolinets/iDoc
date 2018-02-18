cd ./..

pm2 start process.json --name site
pm2 info site

cd ./_
