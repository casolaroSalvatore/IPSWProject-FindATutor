package logic.model.domain.state;

// ConcreteState che rappresenta una sessione rifiutata dal tutor.
// Si tratta di uno stato terminale: non sono previste transizioni successive da qui.
class RefusedState extends AbstractTutoringSessionState {
    // Nessun metodo è sovrascritto: lo stato è terminale e non consente ulteriori eventi o transizioni.
}
