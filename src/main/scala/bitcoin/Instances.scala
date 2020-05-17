/**
  * Licensed to the Apache Software Foundation (ASF) under one or more
  * contributor license agreements.  See the NOTICE file distributed with
  * this work for additional information regarding copyright ownership.
  * The ASF licenses this file to You under the Apache License, Version 2.0
  * (the "License"); you may not use this file except in compliance with
  * the License.  You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
package com.madewithtea.blockchainrpc.bitcoin

import cats.effect.IO
import io.circe.generic.auto._
import com.madewithtea.blockchainrpc.BasicMethods._
import com.madewithtea.blockchainrpc.Codecs._
import com.madewithtea.blockchainrpc.bitcoin.Protocol._
import com.madewithtea.blockchainrpc.bitcoin.Codecs._
import com.madewithtea.blockchainrpc.{BatchRequest, BatchResponse, Bitcoin}

import scala.collection.mutable.ListBuffer

object Instances {

  implicit val getNextBlockHashInstance =
    new GetNextBlockHash[Bitcoin] {
      override def getNextBlockHash(a: Bitcoin): IO[String] =
        a.client.nextBlockHash()
    }

  implicit val getBlockByHashInstance =
    new GetBlockByHash[Bitcoin, BlockResponse] {
      override def getBlockByHash(
          a: Bitcoin,
          hash: String
      ): IO[BlockResponse] = {
        a.client.request[BlockRequest, BlockResponse](BlockRequest(hash))
      }
    }

  implicit val getBlockHashInstance = new GetBlockHash[Bitcoin] {
    override def getBlockHash(a: Bitcoin, height: Long): IO[String] =
      for {
        json <- a.client
          .requestJson[BlockHashRequest](BlockHashRequest(height))
      } yield json.asObject.get("result").get.asString.get
  }

  implicit val getBlockByHeightInstance =
    new GetBlockByHeight[Bitcoin, BlockResponse] {
      override def getBlockByHeight(
          a: Bitcoin,
          height: Long
      ): IO[BlockResponse] =
        for {
          hash <- getBlockHashInstance.getBlockHash(a, height)
          data <- getBlockByHashInstance.getBlockByHash(a, hash)
        } yield data
    }

  implicit val getBestBlockHashInstance =
    new GetBestBlockHash[Bitcoin] {
      override def getBestBlockHash(a: Bitcoin): IO[String] =
        for {
          json <- a.client
            .requestJson[BestBlockHashRequest](new BestBlockHashRequest)
        } yield json.asObject.get("result").get.asString.get
    }

  implicit val getBestBlockHeightInstance =
    new GetBestBlockHeight[Bitcoin] {
      override def getBestBlockHeight(a: Bitcoin): IO[Long] =
        for {
          hash <- getBestBlockHashInstance.getBestBlockHash(a)
          block <- getBlockByHashInstance.getBlockByHash(a, hash)
        } yield block.height
    }

  implicit val getTransactionsInstance =
    new GetTransactions[Bitcoin, BatchResponse[
      TransactionResponse
    ]] {
      override def getTransactions(
          a: Bitcoin,
          hashes: Seq[String]
      ): IO[BatchResponse[TransactionResponse]] = {
        val list = ListBuffer(hashes: _*)
        val genesisTransactionIndex =
          list.indexOf(Transactions.GenesisTransactionHash)

        if (genesisTransactionIndex >= 0) {
          list.remove(genesisTransactionIndex)
        }

        val result =
          a.client.request[BatchRequest[TransactionRequest], BatchResponse[
            TransactionResponse
          ]](
            BatchRequest[TransactionRequest](list.map(TransactionRequest.apply).toSeq)
          )

        if (genesisTransactionIndex >= 0) {
          for {
            batcResponse <- result
            listResult <- IO(ListBuffer(batcResponse.seq: _*))
            _ <- IO(
              listResult
                .insert(
                  genesisTransactionIndex,
                  Transactions.GenesisTransaction
                )
            )
          } yield BatchResponse(listResult.toSeq)
        } else {
          result
        }
      }
    }

  implicit val getTransactionInstance =
    new GetTransaction[Bitcoin, TransactionResponse] {
      override def getTransaction(
          a: Bitcoin,
          hash: String
      ): IO[TransactionResponse] =
        if (hash != Transactions.GenesisTransactionHash) {
          a.client.request[TransactionRequest, TransactionResponse](
            TransactionRequest(hash)
          )
        } else {
          IO(Transactions.GenesisTransaction)
        }
    }

  implicit val estimateSmartFeeInstance =
    new EstimateSmartFee[Bitcoin, FeeResponse] {
      override def estimateSmartFee(a: Bitcoin, height: Long): IO[FeeResponse] =
        a.client.request[FeeRequest, FeeResponse](FeeRequest(height))
    }
}
