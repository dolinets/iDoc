#!/usr/bin/env python

import os, argparse, subprocess

parser = argparse.ArgumentParser()
parser.add_argument('-v', '--version', help='Project version', default="none")
parser.add_argument('-ti', '--tier', help='Project tier', required=False, default='none')
parser.add_argument('-ty', '--type', help='Project type', required=False, default='none')
parser.add_argument('-p', '--project', help='Project name', default=argparse.SUPPRESS)
parser.add_argument('-pa', '--project_additional', help='Project additional name, like wf-region-client', default=argparse.SUPPRESS)
parser.add_argument('-sd', '--skip-deploy', help='Skip deploy', dest='skip-deploy', default='false')
parser.add_argument('-sb', '--skip-build', help='Skip build', dest='skip-build', default='false')
parser.add_argument('-st', '--skip-test', help='Skip tests', dest='skip-test', default='false')
parser.add_argument('-sdoc', '--skip-doc', help='Skip doc', dest='skip-doc', default='false')
parser.add_argument('-dt', '--deploy-timeout', help='Deploy timeout', dest='deploy-timeout', default="200")
parser.add_argument('-c', '--compile', help='Compile', default=argparse.SUPPRESS)
parser.add_argument('-ju', '--jenkins-user', help='Jenkins username', dest='jenkins-user', required=True)
parser.add_argument('-ja', '--jenkins-api', help='jenkins api key', dest='jenkins-api', required=True)
parser.add_argument('-d', '--docker', help='Build with docker', default='false')
parser.add_argument('-do', '--dockerOnly', help='Only build with docker', default='false')
parser.add_argument('-gc', '--gitCommit', help='Git commit', default='none')
parser.add_argument('-bic', '--bBuildInContainer', help='Build in container', default='false')
parser.add_argument('-sysr', '--sysrepo', help='Git System Repository', dest='sysrepo', default="git@iSystem.github.com:e-government-ua/iSystem.git")
parser.add_argument('-sysb', '--sysbranch', help='Git System Repository branch', dest='sysbranch', default="test")
parser.add_argument('-sbr', '--sbranch', help='Project sBranch', dest='sbranch', default="none")

args = parser.parse_args()

commandArr = ["bash", "scripts/deploy_private.sh"]

for arg in vars(args):
    commandArr.append('--'+arg)
    commandArr.append(getattr(args, arg))

if os.path.exists("iSystem"):
    subprocess.call("rm -rf iSystem", shell=True)

branch = args.sysbranch
if args.version == 'prod' or args.version == 'prod-backup' or args.sbranch == 'iGov__Prod'  or args.sbranch == 'iGov__Prod_backup':
   branch = 'master'

subprocess.call("git clone %s -b %s --single-branch iSystem" % (args.sysrepo, branch), shell=True)
subprocess.call("rsync -rt iSystem/scripts/ scripts/", shell=True)
subprocess.call("rm -rf iSystem", shell=True)
subprocess.call("chmod +x scripts/*", shell=True)
subprocess.check_call(commandArr)
