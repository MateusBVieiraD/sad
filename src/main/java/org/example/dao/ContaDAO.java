package org.example.dao;

import jakarta.persistence.*;
import org.example.config.EntityFactory;
import org.example.entity.*;
import java.math.BigDecimal;

public class ContaDAO {

    private final EntityManager entityManager;

    public ContaDAO() {
        // Cria o EntityManagerFactory com base no nome da unidade de persistência
        entityManager = EntityFactory.getEntityManager();
    }

    public int salvar(ContaEntity conta) {
        EntityTransaction transaction = entityManager.getTransaction();
        try {
            transaction.begin();
            entityManager.persist(conta);
            transaction.commit();
            return conta.getId();
        } catch (RuntimeException e) {
            if (transaction.isActive()) {
                transaction.rollback();
                return -1;
            }
            throw e; // Re-throw exception
        }
    }

    public boolean remover(ContaEntity contaEntity){
        EntityTransaction transaction = entityManager.getTransaction();
        try {
            transaction.begin();
            entityManager.remove(contaEntity);
            transaction.commit();
            return true;

        } catch (RuntimeException e) {
            if (transaction.isActive()) {
                transaction.rollback();
                return false;
            }
            throw e; // Re-throw exception
        }
    }

    public static ContaEntity update(ContaEntity conta){
        var a = EntityFactory.getEntityManager();
        EntityTransaction transaction = a.getTransaction();
        try {
            transaction.begin();
            ContaEntity contaAtualizada = a.merge(conta);
            transaction.commit();
            return contaAtualizada;
        } catch (RuntimeException e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            throw new RuntimeException("Falha ao atualizar a conta", e); // Re-throw exception
        }
    }

    public void fechar() {
        entityManager.close();
    }

    public static int verificarContaRelacionada(int clienteId) {
        var a = EntityFactory.getEntityManager();

        TypedQuery<Integer> query = a.createQuery(
                "SELECT co.id FROM ContaEntity co JOIN co.cliente c WHERE c.id = :id",
                Integer.class
        );
        query.setParameter("id", clienteId);

        int c1 = query.getSingleResult();
        int result = c1;
        System.out.println(result);
        return result;

    }

    public int criarConta(int id, String agencia, TipoConta tipoConta){
        ContaEntity conta = new ContaEntity();
        ContaDAO contaDAO = new ContaDAO();
        Cliente cliente = entityManager.find(Cliente.class, id);
        conta.setNumeroConta(String.valueOf(id));
        conta.setAgencia(agencia);
        conta.setTipoconta(tipoConta);
        conta.setCliente(cliente);

        int retornoId = contaDAO.salvar(conta);
        return retornoId;

    }

    public static BigDecimal verificarSaldo(int clienteId) {

        try{
            var a = EntityFactory.getEntityManager();

            TypedQuery<BigDecimal> query = a.createQuery(
                    "SELECT co.saldo FROM ContaEntity co JOIN co.cliente c WHERE c.id = :id",
                    BigDecimal.class
            );

            query.setParameter("id", clienteId);

            BigDecimal c1 = query.getSingleResult();


            return c1;


        } catch (RuntimeException e) {
            throw new RuntimeException(e);
        }

    }


    public static void atualizarSaldo(int id, double saldo){
        EntityManager a = EntityFactory.getEntityManager();
        EntityTransaction transaction = a.getTransaction();

        try {
            transaction.begin();

            ContaEntity contaPraAtualizar = a.find(ContaEntity.class, id);

            contaPraAtualizar.adicionarSaldo(saldo);
            TransacaoDAO transacaoDAO = new TransacaoDAO();
            transacaoDAO.criarTransacao(contaPraAtualizar,saldo,TipoTransacao.DEPOSITO);

            transaction.commit();
            System.out.println("Saldo atualizado com sucesso!");

        } catch (RuntimeException e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            throw new RuntimeException("Erro ao atualizar o saldo da conta: " + e.getMessage(), e);
        } finally {
            a.close(); // Fecha o EntityManager para liberar recursos
        }
    }

    public static boolean sacarSaldo(int id, double valor){
        EntityManager a = EntityFactory.getEntityManager();

        try{

            ContaEntity contaPraSacar = a.find(ContaEntity.class, id);

            if(contaPraSacar != null && valor < contaPraSacar.getSaldo().doubleValue()){
                contaPraSacar.sacarSaldo(valor);

                update(contaPraSacar);

                TransacaoDAO transacaoDAO = new TransacaoDAO();

                transacaoDAO.criarTransacao(contaPraSacar, valor, TipoTransacao.SAQUE);

                return true;

            }

            return false;

        } catch (Exception e) {
            throw new RuntimeException("Algo de errado ocorreu ao sacar o saldo");
        }


    }




    public ContaEntity buscarNumeroConta(String numeroConta){
        try {
            return entityManager.createQuery("SELECT c FROM ContaEntity c WHERE c.numeroConta = :numeroConta", ContaEntity.class)
                    .setParameter("numeroConta", numeroConta)
                    .getSingleResult();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public ContaEntity buscarNumeroConta(int id){
        try {
            return entityManager.createQuery("SELECT c FROM ContaEntity c WHERE c.numeroConta = :idConta", ContaEntity.class)
                    .setParameter("idConta", id)
                    .getSingleResult();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


}
