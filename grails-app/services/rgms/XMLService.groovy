package rgms

import org.springframework.web.multipart.MultipartHttpServletRequest
import org.springframework.web.multipart.commons.CommonsMultipartFile
import javax.*
import rgms.member.*
import rgms.publication.*

class XMLService {

    /*
        saveEntity - closure que salva a classe de domínio que está usando a importação
     */
    static boolean Import(Closure saveEntity, Closure returnWithMessage, String flashMessage, String controller,
        javax.servlet.http.HttpServletRequest request)
    {
        boolean errorFound = false

        try {
            Node xmlFile = parseReceivedFile(request)
            saveEntity(xmlFile)
        }
        //If file is not XML or if no file was uploaded
        catch (SAXParseException) {
            flashMessage = 'default.xml.parserror.message'
            errorFound = true
        }
        //If XML structure is not according to Lattes, it'll perform an invalid cast
        catch (NullPointerException) {
            flashMessage = 'default.xml.structure.message'
            errorFound = true
        }
        catch (Exception) {
            flashMessage = 'default.xml.unknownerror.message'
            errorFound = true
        }

        returnWithMessage(flashMessage, controller)
        return !errorFound
    }

    static void saveFerramentas(Node xmlFile){
        Node producaoTecnica = (Node) xmlFile.children()[2]

        for (Node currentNode : producaoTecnica.children()){
            if (currentNode.name().equals("SOFTWARE"))
                saveNewTool(currentNode)
        }
    }

    private static void saveNewTool(Node currentNode){
        Node dadosBasicos = (Node) currentNode.children()[0]
        Node detalhamentoDoSoftware = (Node) currentNode.children()[1]
        Node informacoesAdicionais = getNodeFromNode(currentNode, "INFORMACOES-ADICIONAIS")

        Ferramenta newTool = new Ferramenta()

        newTool.publicationDate = new Date()
        String tryingToParse = getAttributeValueFromNode(dadosBasicos, "ANO")
        if (tryingToParse.isInteger())
            newTool.publicationDate.set(year: tryingToParse.toInteger())

        newTool.file = 'no File'
        newTool.website = 'no Website'
        newTool.title = getAttributeValueFromNode(dadosBasicos, "TITULO-DO-SOFTWARE")

        String descricao =   getAttributeValueFromNode(informacoesAdicionais, "DESCRICAO-INFORMACOES-ADICIONAIS")
        newTool.description = "País: "+getAttributeValueFromNode(dadosBasicos, "PAIS") +", Ambiente: " + getAttributeValueFromNode(detalhamentoDoSoftware, "AMBIENTE") + (descricao.equals("") ? "" : ", Informacoes adicionais: "+descricao)
        newTool.save(flush: false)
    }

    static void createBooksChapters(Node xmlFile){
        Node bookChapters = (Node) ((Node) ((Node) xmlFile.children()[1]).children()[2]).children()[1]
        List<Object> bookChaptersChildren = bookChapters.children()

        for (int i = 0; i < bookChaptersChildren.size(); ++i){
            List<Object> bookChapter = ((Node) bookChaptersChildren[i]).children()
            Node dadosBasicos = (Node) bookChapter[0]
            Node detalhamentoCapitulo = (Node) bookChapter[1]
            createNewBookChapter(dadosBasicos,detalhamentoCapitulo, i)
        }
    }

    private static void createNewBookChapter (Node dadosBasicos, Node detalhamentoCapitulo, int i){
        BookChapter newBookChapter = new BookChapter()
        newBookChapter.title = getAttributeValueFromNode(dadosBasicos, "TITULO-DO-CAPITULO-DO-LIVRO")
        newBookChapter.publisher = getAttributeValueFromNode(detalhamentoCapitulo, "NOME-DA-EDITORA")

        print(newBookChapter.title)
        if (Publication.findByTitle(newBookChapter.title) == null)
            fillBookChapterInfo(newBookChapter, dadosBasicos, i)
    }

    private static void fillBookChapterInfo (BookChapter newBookChapter, Node dadosBasicos, int i){
        newBookChapter.publicationDate = new Date()
        String tryingToParse = getAttributeValueFromNode(dadosBasicos, "ANO")
        if (tryingToParse.isInteger())
            newBookChapter.publicationDate.set(year: tryingToParse.toInteger())

        print(newBookChapter.publicationDate)
        newBookChapter.file = 'emptyfile' + i.toString()
        newBookChapter.chapter = 2
        newBookChapter.save(flush: false)
    }

    static void createDissertations(Node xmlFile){
        Node dadosGerais = (Node) xmlFile.children()[0]
        Node mestrado = (Node) ((Node) dadosGerais.children()[3]).children()[1]
        Node doutorado = (Node) ((Node) dadosGerais.children()[3]).children()[2]

        createDissertation(mestrado)
        createDissertation(doutorado)
    }

    private static void createDissertation(Node xmlNode) {
        Dissertacao newDissertation = new Dissertacao()
        newDissertation.title = getAttributeValueFromNode(xmlNode, "TITULO-DA-DISSERTACAO-TESE")

        newDissertation.publicationDate = new Date()
        String tryingToParse = getAttributeValueFromNode(xmlNode, "ANO-DE-OBTENCAO-DO-TITULO")
        if (tryingToParse.isInteger())
            newDissertation.publicationDate.set(year: tryingToParse.toInteger())

        newDissertation.school = getAttributeValueFromNode(xmlNode, "NOME-INSTITUICAO")
        newDissertation.file = 'no File'
        newDissertation.address = 'no Address'
        newDissertation.save(flush: false)
    }

    static void createConferencias(Node xmlFile){
        Node trabalhosEmEventos = (Node) ((Node)xmlFile.children()[1]).children()[0]

        for (Node currentNode : trabalhosEmEventos.children()){
            List<Object> nodeConferencia = currentNode.children()
            saveNewConferencia (nodeConferencia);
        }
    }

    private static void saveNewConferencia (List nodeConferencia){
        Node dadosBasicos = (Node) nodeConferencia[0]
        Node detalhamento = (Node) nodeConferencia[1]
        String nomeEvento = getAttributeValueFromNode(detalhamento, "NOME-DO-EVENTO")

        if(nomeEvento.contains("onferenc")){
            Conferencia novaConferencia = new Conferencia()
            novaConferencia.title = nomeEvento

            if (Publication.findByTitle(novaConferencia.title) == null){
                novaConferencia.publicationDate = new Date()
                String tryingToParse = getAttributeValueFromNode(dadosBasicos, "ANO-DO-TRABALHO")
                if (tryingToParse.isInteger())
                    novaConferencia.publicationDate.set(year: tryingToParse.toInteger())

                tryingToParse = getAttributeValueFromNode(dadosBasicos, "TITULO-DO-TRABALHO")
                novaConferencia.booktitle = tryingToParse;
                tryingToParse =  getAttributeValueFromNode(detalhamento, "PAGINA-INICIAL")
                String tryingToParse2 = getAttributeValueFromNode(detalhamento, "PAGINA-FINAL")
                novaConferencia.pages = tryingToParse + " - " + tryingToParse2
                novaConferencia.file = 'emptyfile'
                novaConferencia.save(flush: false)
            }
        }
    }

    static void createOrientations(Node xmlFile){
        List<Object> completedOrientations = findCompletedOrientations(xmlFile)
        Member user = Member.findByUsername(session.getAttribute("username").toString())

        if (!XMLService.isNullOrEmpty(completedOrientations))
            for (int i = 0; i < completedOrientations.size(); i++)
                saveNewOrientation(completedOrientations, i, user)
    }

    private static void saveNewOrientation(List<Object> completedOrientations, int i, Member user) {
        Node node = (Node) completedOrientations[i]
        Orientation newOrientation = new Orientation()
        String name = (String) node.name()

        fillNewOrientation(name, node, newOrientation, user)
        saveOrientation(newOrientation)
    }

    private static void fillNewOrientation(String name, Node node, Orientation newOrientation, Member user){
        if (name.toLowerCase().contains("mestrado")) {
            fillOrientationData(node, newOrientation, user, "Mestrado")
        } else if (name.toLowerCase().contains("doutorado")) {
            fillOrientationData(node, newOrientation, user, "Doutorado")
        } else {
            Node children = (Node) (node.children()[0])
            String natureza = (String) children.attribute("NATUREZA")

            if (isUndergraduateResearch(natureza)) {
                fillOrientationData(node, newOrientation, user, "Iniciação Científica")
            }
        }
    }

    //Only saves if the orientation does not already exist
    private static void saveOrientation(Orientation newOrientation) {
        if (Orientation.findAll().find { it -> newOrientation.Equals(it) } == null)
            newOrientation.save(flush: false)
    }

    private static boolean isUndergraduateResearch(String natureza) {
        natureza.toLowerCase().contains("iniciacao_cientifica")
    }

    private static boolean isNullOrEmpty(List<Object> completedOrientations) {
        completedOrientations == null || completedOrientations.size() > 0
    }

    private static List<Object> findCompletedOrientations(Node xmlFile) {
        def orientations = (Node) xmlFile.children()[3]
        def completedOrientations = (Node) orientations.children()[0]
        List<Object> values = completedOrientations.children()
        values
    }

    private static void fillOrientationData(Node node, Orientation newOrientation, Member user, String tipoOrientacao) {
        Node basicData = (Node)(node.children()[0])
        Node specificData = (Node)(node.children()[1])
        newOrientation.tipo = tipoOrientacao
        newOrientation.tituloTese = getAttributeValueFromNode(basicData, "TITULO")
        String ano = getAttributeValueFromNode(basicData, "ANO")
        newOrientation.anoPublicacao = Integer.parseInt(ano)
        newOrientation.curso = getAttributeValueFromNode(specificData, "NOME-DO-CURSO")
        newOrientation.instituicao = getAttributeValueFromNode(specificData, "NOME-DA-INSTITUICAO")
        newOrientation.orientador = user
        newOrientation.orientando = getAttributeValueFromNode(specificData, "NOME-DO-ORIENTADO")
    }

    static void createJournals(Node xmlFile){
        Node artigosPublicados = (Node) ((Node) xmlFile.children()[1]).children()[1]
        List<Object> artigosPublicadosChildren = artigosPublicados.children()

        for (int i = 0; i < artigosPublicadosChildren.size(); ++i)
            saveNewJournal(artigosPublicadosChildren, i)
    }

    private static void saveNewJournal(List artigosPublicadosChildren, int i) {
        List<Object> firstArticle = ((Node) artigosPublicadosChildren[i]).children()
        Node dadosBasicos = (Node) firstArticle[0]
        Node detalhamentoArtigo = (Node) firstArticle[1]
        Periodico newJournal = new Periodico()
        getJournalTitle(dadosBasicos, newJournal)

        if (Publication.findByTitle(newJournal.title) == null) {
            getJournalYear(dadosBasicos, newJournal)
            getJournalVolume(detalhamentoArtigo, newJournal)
            getJournalNumber(detalhamentoArtigo, newJournal)
            getJournalNumberOfPages(detalhamentoArtigo, newJournal)
            getPeriodicTitle(detalhamentoArtigo, newJournal)
            newJournal.file = 'emptyfile' + i.toString() //files are not available on lattes
            newJournal.save(flush: false)
        }
    }

    private static void getJournalTitle(Node dadosBasicos, Periodico newJournal) {
        newJournal.title = getAttributeValueFromNode(dadosBasicos, "TITULO-DO-ARTIGO")
    }

    private static void getPeriodicTitle(Node detalhamentoArtigo, Periodico newJournal) {
        newJournal.journal = getAttributeValueFromNode(detalhamentoArtigo, "TITULO-DO-PERIODICO-OU-REVISTA")
    }

    private static void getJournalNumberOfPages(Node detalhamentoArtigo, Periodico newJournal) {
        String tryingToParse = getAttributeValueFromNode(detalhamentoArtigo, "PAGINA-FINAL")
        String tryingToParse2 = getAttributeValueFromNode(detalhamentoArtigo, "PAGINA-INICIAL")
        if (tryingToParse.isInteger() && tryingToParse2.isInteger())
            newJournal.pages = tryingToParse.toInteger() - tryingToParse2.toInteger() + 1
        else
            newJournal.pages = 1    //if not parsed successfully, least value possible
    }

    private static void getJournalNumber(Node detalhamentoArtigo, Periodico newJournal) {
        String tryingToParse = getAttributeValueFromNode(detalhamentoArtigo, "FASCICULO")
        if (tryingToParse.isInteger())
            newJournal.number = tryingToParse.toInteger()
        else
            newJournal.number = 1   //if not parsed successfully, least value possible
    }

    private static void getJournalVolume(Node detalhamentoArtigo, Periodico newJournal) {
        String tryingToParse = getAttributeValueFromNode(detalhamentoArtigo, "VOLUME")
        if (tryingToParse.isInteger())
            newJournal.volume = tryingToParse.toInteger()
        else
            newJournal.volume = 1   //if not parsed successfully, least value possible
    }

    private static void getJournalYear(Node dadosBasicos, Periodico newJournal) {
        newJournal.publicationDate = new Date()
        String tryingToParse = getAttributeValueFromNode(dadosBasicos, "ANO-DO-ARTIGO")
        if (tryingToParse.isInteger())
            newJournal.publicationDate.set(year: tryingToParse.toInteger() - 1900)
    }

    static Node parseReceivedFile(MultipartHttpServletRequest request) {
        MultipartHttpServletRequest mpr = (MultipartHttpServletRequest) request;
        CommonsMultipartFile f = (CommonsMultipartFile) mpr.getFile("file");
        File file = new File("xmlimported.xml");
        f.transferTo(file)
        def records = new XmlParser()

        if (file.length()>0)
            records.parse(file)
    }

    static String getAttributeValueFromNode(Node n, String attribute) {
        n.attribute attribute
    }

    static Node getNodeFromNode(Node n, String nodeName){
        for(Node currentNodeChild : n.children()){
            if ((currentNodeChild.name()+"").equals((nodeName)))
                return currentNodeChild
        }
    }
}
