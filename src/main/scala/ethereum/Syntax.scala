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
package com.madewithtea.blockchainrpc.ethereum

import com.madewithtea.blockchainrpc.BasicMethods._
import com.madewithtea.blockchainrpc.{BatchResponse, Ethereum}
import com.madewithtea.blockchainrpc.ethereum.Instances._
import com.madewithtea.blockchainrpc.ethereum.Methods._
import com.madewithtea.blockchainrpc.ethereum.Protocol.{BlockResponse, _}

object Syntax {
  implicit class EthereumOps(b: Ethereum) {
    
    def getReceiptByHash(hash: String) =
      implicitly[GetReceipt[Ethereum, ReceiptResponse]].getReceipt(b, hash)

    def getReceiptsByHash(hashes: Seq[String]) =
      implicitly[GetReceipts[Ethereum, BatchResponse[ReceiptResponse]]]
        .getReceipts(b, hashes)

    def getBlockByHeight(height: Long) =
      implicitly[GetBlockByHeight[Ethereum, BlockResponse]]
        .getBlockByHeight(b, height)

    def getBlockByHash(hash: String) =
      implicitly[GetBlockByHash[Ethereum, BlockResponse]]
        .getBlockByHash(b, hash)

    def getBestBlockHeight() =
      implicitly[GetBestBlockHeight[Ethereum]].getBestBlockHeight(b)

    def getTransaction(hash: String) =
      implicitly[GetTransaction[Ethereum, TransactionResponse]]
        .getTransaction(b, hash)

    def getTransactions(hashes: Seq[String]) =
      implicitly[GetTransactions[Ethereum, BatchResponse[TransactionResponse]]]
        .getTransactions(b, hashes)
  }
}
