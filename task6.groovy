job('T6JOB1') {
    description('Downloads GitHub code')
    scm {
         github('vikashkr437/TestCodesDevOpsT3','master')
    }
    triggers {
	scm(" * * * * * ")        
    }
    steps {
           shell(' cp -rf * /ws/ ')
    }
}




job('T6JOB2') {
   triggers {
      upstream('T6JOB1' , 'SUCCESS')
   }
   steps {
   remoteShell('root@192.168.99.104:22') {
      command('''
      if (sudo ls /root/wst3 | sudo grep html )
      then
      if  kubectl get deployment  myweb
      then
      sudo echo "Already Running Requirement satisfied"
      else  
      kubectl create -f /root/dockjen/htmldep.yml
      fi
      else
      sudo echo "Sorry Requirement cannot be satisfied"
      fi
      ''')
      }
   }
}



job('T6JOB3') {
    triggers {
        upstream('T6JOB2' , 'SUCCESS')
    }
    steps {
        remoteShell('root@192.168.99.104:22') {
            command('''
            stat=$(curl -o /dev/null -s -w "%{http_code}" http://192.168.99.103:30001)
            if [[ $stat==200 ]]
            then 
            echo "Code is correct"
            else 
            echo "Incorrect Code Sending mail to developer"
            curl http://192.168.99.104:9998/job/job4/build?token=redhat
            fi
            ''')
         }
     }
}


job('T6JOB4') {
    authenticationToken('redhat')
    publishers {
        extendedEmail {
            recipientList('vikashkumar43723@gmail.com')
            defaultSubject('Oops')
            defaultContent('Something broken')
            contentType('text/html')
            triggers {
                always {
                    subject('Build Failed ')
                    content('There is some problem with the code.')
                    sendTo {
                        recipientList()
                    }
                }
            }
        }
    }
}
buildPipelineView('DevOps-Task6') {
    filterBuildQueue()
    filterExecutors()
    title('DevOps AL Task6 Build Pipeline')
    displayedBuilds(1)
    selectedJob('T6JOB1')
    showPipelineParameters(true)
}
