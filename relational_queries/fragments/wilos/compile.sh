# compile the original source files and dump the normalized code
# first disable the GenerateUDT, ComputeVC, and DumpVC jobs in MLScheduler

#javac -cp \
#~/build/polyglot-2.7.1-src/bin/jl7c -d /tmp -classpath \

~/proj/metalift/scripts/ml_java_frontend.sh -classpath \
../jar/wilos-after.jar:\
../jar/Wilos-core-services.jar:\
../jar/activation.jar:\
../jar/antlr-2.7.6.jar:\
../jar/asm-attrs.jar:\
../jar/asm.jar:\
../jar/backport-util-concurrent.jar:\
../jar/cglib-2.1.3.jar:\
../jar/commons-beanutils-1.7.0.jar:\
../jar/commons-codec-1.3.jar:\
../jar/commons-collections-2.1.1.jar:\
../jar/commons-dbcp-1.1.jar:\
../jar/commons-digester.jar:\
../jar/commons-discovery-0.2.jar:\
../jar/commons-el.jar:\
../jar/commons-fileupload.jar:\
../jar/commons-lang-2.1.jar:\
../jar/commons-logging-1.0.4.jar:\
../jar/commons-pool.jar:\
../jar/dom4j-1.6.1.jar:\
../jar/hibernate3.jar:\
../jar/hsqldb.jar:\
../jar/icefaces-comps.jar:\
../jar/icefaces.jar:\
../jar/jaxb-api.jar:\
../jar/jaxb-impl.jar:\
../jar/jaxb-xjc.jar:\
../jar/jaxb1-impl.jar:\
../jar/jaxws-api-2.0.jar:\
../jar/jaxws-api.jar:\
../jar/jaxws-rt.jar:\
../jar/jdom-1.0.jar:\
../jar/jsf-api.jar:\
../jar/jsf-impl.jar:\
../jar/jsr173_api.jar:\
../jar/jsr181-api.jar:\
../jar/jsr250-api.jar:\
../jar/jstl.jar:\
../jar/jta.jar:\
../jar/krysalis-jCharts-1.0.0-alpha-1.jar:\
../jar/log4j-1.2.14.jar:\
../jar/mail.jar:\
../jar/mysql-connector-java-5.1.5-bin.jar:\
../jar/resolver.jar:\
../jar/saaj-api.jar:\
../jar/saaj-impl.jar:\
../jar/servlet-api.jar:\
../jar/sjsxp.jar:\
../jar/spring.jar:\
../jar/stax-api-1.0.1.jar:\
../jar/stax-ex.jar:\
../jar/stax-utils-20040917.jar:\
../jar/streambuffer.jar:\
../jar/swingx-0.9.0.jar:\
../jar/wilosassistant-webservices-client-generated.jar:\
../jar/wsdl4j-1.6.1.jar:\
../jar/wss4j-1.5.1.jar:\
../jar/wstx-asl-3.2.0.jar:\
../jar/xbean-2.2.0.jar:\
../jar/xbean-spring-2.8.jar:\
../jar/xercesImpl-2.6.2.jar:\
../jar/xfire-all-1.2.6.jar:\
../jar/xfire-jsr181-api-1.0-M1.jar:\
../jar/xml-apis-1.3.02.jar:\
../jar/xpp3_min-1.1.3.4.O.jar:\
../jar/xstream-1.2.2.jar \
-print normalizeLoop \
./wilos/business/services/spem2/activity/ActivityService.java \
./wilos/business/services/misc/concreteactivity/ConcreteActivityService.java \
./wilos/business/services/misc/concreterole/ConcreteRoleAffectationService.java \
./wilos/business/services/misc/concreterole/ConcreteRoleDescriptorService.java \
./wilos/business/services/misc/concreteworkbreakdownelement/ConcreteWorkBreakdownElementService.java \
./wilos/business/services/misc/concreteworkproduct/ConcreteWorkProductDescriptorService.java \
./wilos/business/services/misc/project/ProjectService.java \
./wilos/business/services/misc/wilosuser/LoginService.java \
./wilos/business/services/misc/wilosuser/ParticipantService.java \
./wilos/business/services/misc/wilosuser/RoleService.java \
./wilos/business/services/spem2/activity/ActivityService.java \
./wilos/business/services/spem2/guide/GuidanceService.java \
./wilos/business/services/spem2/iteration/IterationService.java \
./wilos/business/services/spem2/phase/PhaseService.java \
./wilos/hibernate/misc/concreteactivity/ConcreteActivityDao.java \
./wilos/hibernate/misc/project/AffectedtoDao.java \
./wilos/hibernate/misc/wilosuser/RoleDao.java \
./wilos/presentation/web/expandabletable/WorkProductsExpTableBean.java \
./wilos/presentation/web/process/ProcessBean.java \
./wilos/presentation/web/wilosuser/ProcessManagerBean.java \
./wilos/presentation/web/wilosuser/ParticipantBean.java \
./wilos/presentation/web/wilosuser/WilosUserBean.java


#-print normalizeLoop \
