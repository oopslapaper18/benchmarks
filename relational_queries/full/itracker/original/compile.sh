# compile the original source files and dump the normalized code
# first disable the GenerateUDT, ComputeVC, and DumpVC jobs in MLScheduler

#javac -cp "../jar/*" 
#~/build/polyglot-2.7.1-src/bin/jl7c -d /tmp -classpath \

~/proj/metalift/scripts/ml_java_frontend.sh -classpath \
../jar/antlr-2.7.2.jar:\
../jar/aopalliance-1.0.jar:\
../jar/asm-1.5.3.jar:\
../jar/asm-attrs-1.5.3.jar:\
../jar/bcmail-jdk14-136.jar:\
../jar/bcprov-jdk14-136.jar:\
../jar/bsh-1.3.0.jar:\
../jar/c3p0-0.9.1.2.jar:\
../jar/cglib-2.1_3.jar:\
../jar/commons-beanutils-1.7.0.jar:\
../jar/commons-collections-2.1.jar:\
../jar/commons-digester-2.0.jar:\
../jar/commons-fileupload-1.0.jar:\
../jar/commons-lang-2.1.jar:\
../jar/commons-logging-1.0.4.jar:\
../jar/commons-validator-1.1.4.jar:\
../jar/derby-10.3.2.1.jar:\
../jar/dom4j-1.6.1.jar:\
../jar/ehcache-1.2.3.jar:\
../jar/geronimo-jta_1.0.1B_spec-1.0.1.jar:\
../jar/hibernate-3.2.6.ga.jar:\
../jar/itext-2.1.0.jar:\
../jar/itracker.jar:\
../jar/jasperreports-3.1.2.jar:\
../jar/javax.mail.jar:\
../jar/javax.servlet.jar:\
../jar/jcommon-1.0.12.jar:\
../jar/jdtcore-3.1.0.jar:\
../jar/jfreechart-1.0.9.jar:\
../jar/jstl-1.2.jar:\
../jar/log4j-1.2.13.jar:\
../jar/mysql-connector-java-5.0.3.jar:\
../jar/oro-2.0.7.jar:\
../jar/postgresql-8.2-507.jdbc4.jar:\
../jar/quartz-1.5.2.jar:\
../jar/spring-2.0.jar:\
../jar/spring-aop-2.0.jar:\
../jar/spring-beans-2.0.jar:\
../jar/spring-context-2.0.jar:\
../jar/spring-core-2.0.jar:\
../jar/spring-dao-2.0.jar:\
../jar/spring-jdbc-2.0.jar:\
../jar/spring-jpa-2.0.jar:\
../jar/spring-mock-2.0.jar:\
../jar/spring-support-2.0.jar:\
../jar/spring-web-2.0.jar:\
../jar/spring-webmvc-2.0.jar:\
../jar/standard-1.1.2.jar:\
../jar/struts-1.2.9.jar:\
../jar/xml-apis-1.3.02.jar \
-print normalizeLoop \
./org/itracker/services/implementations/NotificationServiceImpl.java \
./org/itracker/services/implementations/IssueServiceImpl.java \
./org/itracker/services/implementations/UserServiceImpl.java \
./org/itracker/web/actions/admin/project/EditProjectFormActionUtil.java \
./org/itracker/web/actions/project/ListProjectsAction.java \
./org/itracker/web/actions/project/MoveIssueFormAction.java

# polyglot has problems with including this jar
#../jar/xalan-2.5.1.jar:\
